package dev.jj.contact;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Configurazione "contact.*" validata all'avvio: se un valore è mancante o insicuro
 * il servizio NON parte (fail-fast), invece di girare con impostazioni deboli.
 */
@Validated
@ConfigurationProperties("contact")
public record ContactProperties(
        @NotBlank(message = "CONTACT_IP_SALT mancante")
        @Size(min = 16, message = "CONTACT_IP_SALT deve avere almeno 16 caratteri (es. openssl rand -hex 32)")
        @Pattern(regexp = "^(?!change-me).*", message = "CONTACT_IP_SALT è ancora il valore d'esempio: generane uno vero")
        String ipSalt,
        @NotNull @Valid RateLimit rateLimit,
        @NotNull Duration retention,
        @NotNull @Valid Notification notification) {

    /** Al massimo perIp messaggi per IP e global messaggi in totale nella finestra. */
    public record RateLimit(@Positive int perIp, @Positive int global, @NotNull Duration window) {
    }

    public record Notification(@Positive int maxAttempts, String to, String from) {
    }
}
