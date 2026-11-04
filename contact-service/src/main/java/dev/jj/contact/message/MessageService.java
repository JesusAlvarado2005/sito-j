package dev.jj.contact.message;

import dev.jj.contact.ContactProperties;
import dev.jj.contact.captcha.TurnstileVerifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
class MessageService {

    /** Solo caratteri possibili in un IPv4/IPv6 testuale. Nessuna risoluzione DNS (InetAddress.getByName la farebbe). */
    private static final Pattern IP_LITERAL = Pattern.compile("[0-9a-fA-F:.]{2,45}");

    private final MessageRepository messages;
    private final TurnstileVerifier turnstile;
    private final ContactProperties props;

    MessageService(MessageRepository messages, TurnstileVerifier turnstile, ContactProperties props) {
        this.messages = messages;
        this.turnstile = turnstile;
        this.props = props;
    }

    /**
     * Ordine dei controlli: rate limit (economico, sul DB) -> anti-bot (chiamata a Cloudflare) -> salvataggio.
     * L'email parte poi dal job di notifica (pattern outbox, vedi NotificationService).
     */
    MessageResponse create(MessageRequest req, String clientIpHeader) {
        String clientIp = validIpOrNull(clientIpHeader);
        String ipHash = hmac(clientIp == null ? "unknown" : clientIp);

        checkRateLimit(ipHash);
        verifyCaptcha(req.captchaToken(), clientIp);

        ContactMessage m = new ContactMessage();
        m.id = UUID.randomUUID();
        m.name = req.name().strip();
        m.email = req.email().strip();
        m.subject = req.subject().strip();
        m.body = req.body().strip();
        m.lang = req.lang();
        m.ipHash = ipHash;
        m.status = MessageStatus.NEW;
        m.createdAt = Instant.now();
        messages.save(m);
        return new MessageResponse(m.id);
    }

    /**
     * Limite "morbido": due richieste simultanee possono superarlo di poco (nessun lock).
     * Va bene come difesa in profondità: il limite rigido è in web-public (FormRateLimiter).
     */
    private void checkRateLimit(String ipHash) {
        ContactProperties.RateLimit limit = props.rateLimit();
        Instant since = Instant.now().minus(limit.window());
        if (messages.countByIpHashAndCreatedAtAfter(ipHash, since) >= limit.perIp()
                || messages.countByCreatedAtAfter(since) >= limit.global()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many messages, try again later");
        }
    }

    private void verifyCaptcha(String token, String clientIp) {
        boolean ok;
        try {
            ok = turnstile.verify(token, clientIp);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Captcha verification unavailable");
        }
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Captcha verification failed");
        }
    }

    /**
     * L'header arriva da web-public, che lo costruisce da sé con l'IP reale (mai quello inviato dal browser).
     * Qui scartiamo comunque ciò che non sembra un IP, così non finisce nell'hash né a Cloudflare.
     */
    static String validIpOrNull(String header) {
        if (header == null) {
            return null;
        }
        String ip = header.strip();
        return IP_LITERAL.matcher(ip).matches() && (ip.contains(".") || ip.contains(":")) ? ip : null;
    }

    /**
     * HMAC-SHA256(chiave = ip-salt, IP) in esadecimale. È la costruzione corretta per un hash con chiave:
     * senza la chiave non si possono ricalcolare gli hash dei ~4 miliardi di IPv4 per risalire all'IP.
     */
    private String hmac(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");   // Mac non è thread-safe: un'istanza per chiamata
            mac.init(new SecretKeySpec(props.ipSalt().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);       // HmacSHA256 esiste in ogni JVM
        }
    }
}
