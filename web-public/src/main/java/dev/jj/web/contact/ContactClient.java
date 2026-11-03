package dev.jj.web.contact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/** Inoltra il messaggio a contact-service (rete interna). */
@Component
public class ContactClient {

    private static final Logger log = LoggerFactory.getLogger(ContactClient.class);

    public enum Result { SENT, CAPTCHA_FAILED, RATE_LIMITED, UNAVAILABLE }

    record MessageRequest(String name, String email, String subject, String body, String lang, String captchaToken) {
    }

    private final RestClient http;

    @Autowired
    public ContactClient(RestClient.Builder builder, ContactFormProperties props) {
        this(builder.clone().baseUrl(props.serviceUrl()).requestFactory(timeouts()).build());
    }

    /** Per i test: permette di passare un RestClient collegato a MockRestServiceServer. */
    ContactClient(RestClient http) {
        this.http = http;
    }

    /** Timeout dedicati: contact-service a sua volta chiama Cloudflare (fino a 10 s), quindi qui si aspetta un po' di più. */
    private static SimpleClientHttpRequestFactory timeouts() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(15));
        return factory;
    }

    /**
     * @param clientIp IP del visitatore come lo vede web-public (già ricavato in modo affidabile da Tomcat).
     *                 È l'UNICO valore di X-Client-Ip che contact-service riceve: quello inviato dal browser non passa mai.
     */
    public Result send(ContactForm form, String lang, String captchaToken, String clientIp) {
        try {
            http.post().uri("/api/v1/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Client-Ip", clientIp)
                    .body(new MessageRequest(form.getName(), form.getEmail(), form.getSubject(), form.getBody(), lang, captchaToken))
                    .retrieve().toBodilessEntity();
            return Result.SENT;
        } catch (HttpStatusCodeException e) {
            return switch (e.getStatusCode().value()) {
                case 400 -> Result.CAPTCHA_FAILED;   // i campi sono già validati qui: un 400 è quasi sempre il token anti-bot
                case 429 -> Result.RATE_LIMITED;
                default -> {
                    log.warn("contact-service ha risposto {}", e.getStatusCode());
                    yield Result.UNAVAILABLE;
                }
            };
        } catch (RestClientException e) {
            log.warn("contact-service non raggiungibile: {}", e.toString());
            return Result.UNAVAILABLE;
        }
    }
}
