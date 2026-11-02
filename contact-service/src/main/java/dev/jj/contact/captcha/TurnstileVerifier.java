package dev.jj.contact.captcha;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/** Verifica il token anti-bot di Cloudflare Turnstile (gratuito). */
@Component
public class TurnstileVerifier {

    private final RestClient http;
    private final String secret;
    private final String url;

    public TurnstileVerifier(RestClient.Builder builder,
                             @Value("${contact.turnstile.secret:}") String secret,
                             @Value("${contact.turnstile.url}") String url) {
        this.http = builder.build();
        this.secret = secret;
        this.url = url;
    }

    /**
     * @return true se Cloudflare conferma il token. Se manca il secret risponde sempre false (fail-closed).
     * @throws org.springframework.web.client.RestClientException se Cloudflare non è raggiungibile
     */
    public boolean verify(String token, String clientIp) {
        if (!StringUtils.hasText(secret) || !StringUtils.hasText(token)) {
            return false;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", secret);
        form.add("response", token);
        if (StringUtils.hasText(clientIp)) {
            form.add("remoteip", clientIp);
        }
        Result result = http.post().uri(url).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form).retrieve().body(Result.class);
        return result != null && result.success();
    }

    private record Result(boolean success) {
    }
}
