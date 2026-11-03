package dev.jj.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Configuration
class WebConfig {

    /**
     * La lingua si decide dal primo segmento dell'URL (/it, /en): niente cookie né sessione,
     * così ogni URL mostra sempre la stessa lingua (serve alla SEO). Il nome del bean deve essere "localeResolver".
     */
    @Bean
    LocaleResolver localeResolver() {
        return new LocaleResolver() {
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                String uri = request.getRequestURI();
                return uri.equals("/it") || uri.startsWith("/it/") ? Locale.ITALIAN : Locale.ENGLISH;
            }

            @Override
            public void setLocale(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Locale locale) {
                throw new UnsupportedOperationException("La lingua dipende dall'URL");
            }
        };
    }
}
