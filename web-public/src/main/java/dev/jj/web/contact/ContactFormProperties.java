package dev.jj.web.contact;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Configurazione del form contatti, validata all'avvio.
 * La site key Turnstile non è segreta (finisce nell'HTML), ma è obbligatoria: senza, il form non può funzionare.
 */
@Validated
@ConfigurationProperties("site.contact")
public record ContactFormProperties(
        @NotBlank String serviceUrl,
        @NotBlank String turnstileSiteKey,
        /** Invii accettati per IP nella finestra (limite rigido, prima di chiamare contact-service). */
        @DefaultValue("5") @Min(1) int perIp,
        /** Invii accettati in totale nella finestra: protegge la casella email da un attacco distribuito. */
        @DefaultValue("50") @Min(1) int global,
        @DefaultValue("PT1H") @NotNull Duration window,
        /** Quanti IP diversi si tengono in memoria al massimo: oltre, il form rifiuta (fail-closed) invece di riempire la RAM. */
        @DefaultValue("10000") @Min(100) int maxTrackedIps) {
}
