package dev.jj.contact.message;

import dev.jj.contact.captcha.TurnstileVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
class MessageService {

    private final MessageRepository messages;
    private final TurnstileVerifier turnstile;
    private final String ipSalt;

    MessageService(MessageRepository messages, TurnstileVerifier turnstile,
                   @Value("${contact.ip-salt}") String ipSalt) {
        this.messages = messages;
        this.turnstile = turnstile;
        this.ipSalt = ipSalt;
    }

    /** Verifica l'anti-bot, salva il messaggio. L'email parte poi dal job di notifica (vedi NotificationService). */
    MessageResponse create(MessageRequest req, String clientIp) {
        verifyCaptcha(req.captchaToken(), clientIp);

        ContactMessage m = new ContactMessage();
        m.id = UUID.randomUUID();
        m.name = req.name().strip();
        m.email = req.email().strip();
        m.subject = req.subject().strip();
        m.body = req.body().strip();
        m.lang = req.lang();
        m.ipHash = hashIp(clientIp);
        m.status = MessageStatus.NEW;
        m.createdAt = Instant.now();
        messages.save(m);
        return new MessageResponse(m.id);
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

    /** SHA-256(sale + ":" + IP) in esadecimale: permette di riconoscere gli abusi senza conservare l'IP. */
    private String hashIp(String ip) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((ipSalt + ":" + (ip == null ? "unknown" : ip)).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);   // SHA-256 esiste in ogni JVM
        }
    }
}
