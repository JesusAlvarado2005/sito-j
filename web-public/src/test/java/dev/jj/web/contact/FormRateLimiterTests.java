package dev.jj.web.contact;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class FormRateLimiterTests {

    /** Orologio che si può far avanzare a mano. */
    static class MutableClock extends Clock {
        Instant now = Instant.parse("2026-11-03T10:00:00Z");
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    @Test
    void bloccaOltreIlLimitePerIpESiAzzeraDopoLaFinestra() {
        MutableClock clock = new MutableClock();
        FormRateLimiter limiter = new FormRateLimiter(3, 100, Duration.ofHours(1), 1000, clock);
        assertThat(limiter.tryAcquire("1.1.1.1")).isTrue();
        assertThat(limiter.tryAcquire("1.1.1.1")).isTrue();
        assertThat(limiter.tryAcquire("1.1.1.1")).isTrue();
        assertThat(limiter.tryAcquire("1.1.1.1")).isFalse();
        assertThat(limiter.tryAcquire("2.2.2.2")).isTrue();   // un altro IP non è toccato
        clock.now = clock.now.plus(Duration.ofHours(1));
        assertThat(limiter.tryAcquire("1.1.1.1")).isTrue();
    }

    @Test
    void ilTettoGlobaleFermaUnAttaccoDaTantiIp() {
        FormRateLimiter limiter = new FormRateLimiter(5, 3, Duration.ofHours(1), 1000, new MutableClock());
        assertThat(limiter.tryAcquire("1.0.0.1")).isTrue();
        assertThat(limiter.tryAcquire("1.0.0.2")).isTrue();
        assertThat(limiter.tryAcquire("1.0.0.3")).isTrue();
        assertThat(limiter.tryAcquire("1.0.0.4")).isFalse();
    }

    @Test
    void conTroppiIpInMemoriaRifiutaInveceDiCrescere() {
        MutableClock clock = new MutableClock();
        FormRateLimiter limiter = new FormRateLimiter(5, 10_000, Duration.ofHours(1), 100, clock);
        for (int i = 0; i < 100; i++) {
            assertThat(limiter.tryAcquire("10.0.0." + i)).isTrue();
        }
        assertThat(limiter.tryAcquire("10.0.1.1")).isFalse();
        clock.now = clock.now.plus(Duration.ofHours(1));     // scadute: si fa pulizia e c'è di nuovo posto
        assertThat(limiter.tryAcquire("10.0.1.1")).isTrue();
    }

    @Test
    void richiesteSimultaneeNonSuperanoIlLimite() throws Exception {
        FormRateLimiter limiter = new FormRateLimiter(5, 100, Duration.ofHours(1), 1000, new MutableClock());
        AtomicInteger accepted = new AtomicInteger();
        try (ExecutorService pool = Executors.newFixedThreadPool(16)) {
            for (int i = 0; i < 200; i++) {
                pool.submit(() -> { if (limiter.tryAcquire("6.6.6.6")) accepted.incrementAndGet(); });
            }
        }
        assertThat(accepted).hasValue(5);
    }
}
