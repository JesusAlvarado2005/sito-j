package dev.jj.web;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/** Configurazione validata all'avvio: un URL mancante fa fallire subito l'avvio, non la prima richiesta. */
@Validated
@ConfigurationProperties("site")
public record WebProperties(
        /** URL pubblico del sito, senza slash finale: serve a canonical, hreflang e sitemap. */
        @NotBlank String baseUrl,
        @NotBlank String profileServiceUrl,
        @NotBlank String projectsServiceUrl,
        /** Per quanto tempo si riusa la risposta di un servizio prima di richiederla di nuovo. */
        @DefaultValue("PT5M") Duration cacheTtl,
        /**
         * true solo per generare la copia statica (GitHub Pages): niente form, che senza backend non può funzionare,
         * e informativa privacy adattata. Il sito "vero" (Docker/Oracle) resta false.
         */
        @DefaultValue("false") boolean staticMode) {
}
