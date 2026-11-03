package dev.jj.web.client;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class StaleCacheTests {

    @Test
    void riusaIlDatoFinoAllaScadenzaDelTtl() {
        StaleCache<String, String> cache = new StaleCache<>(Duration.ofMinutes(5));
        AtomicInteger calls = new AtomicInteger();
        cache.get("k", () -> "v" + calls.incrementAndGet());
        assertThat(cache.get("k", () -> "v" + calls.incrementAndGet())).contains("v1");
        assertThat(calls).hasValue(1);
    }

    @Test
    void seIlServizioEGiuUsaLUltimoDatoBuono() {
        StaleCache<String, String> cache = new StaleCache<>(Duration.ZERO);   // ttl zero: ricarica sempre
        cache.get("k", () -> "buono");
        Optional<String> r = cache.get("k", () -> {
            throw new IllegalStateException("servizio giù");
        });
        assertThat(r).contains("buono");
    }

    @Test
    void senzaDatoPrecedenteRestituisceVuotoSenzaEccezioni() {
        StaleCache<String, String> cache = new StaleCache<>(Duration.ZERO);
        assertThat(cache.get("k", () -> {
            throw new IllegalStateException("servizio giù");
        })).isEmpty();
    }
}
