package dev.jj.web.contact;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Rate limit RIGIDO sugli invii del form: finestra fissa per IP + un tetto globale.
 * È synchronized, quindi due richieste simultanee non possono superare il limite (a differenza del conteggio
 * sul DB in contact-service, che resta come difesa in profondità).
 * Vive in memoria: un riavvio azzera i contatori, accettabile per un sito con una sola istanza.
 */
public class FormRateLimiter {

    private record Window(Instant start, int count) {
    }

    private final int perIp;
    private final int global;
    private final Duration window;
    private final int maxTrackedIps;
    private final Clock clock;

    private final Map<String, Window> byIp = new HashMap<>();
    private Window globalWindow;

    public FormRateLimiter(int perIp, int global, Duration window, int maxTrackedIps, Clock clock) {
        this.perIp = perIp;
        this.global = global;
        this.window = window;
        this.maxTrackedIps = maxTrackedIps;
        this.clock = clock;
    }

    /** @return true se l'invio è permesso (e viene conteggiato), false se va rifiutato con 429. */
    public synchronized boolean tryAcquire(String ip) {
        Instant now = clock.instant();
        if (globalWindow == null || expired(globalWindow, now)) {
            globalWindow = new Window(now, 0);
        }
        Window w = byIp.get(ip);
        if (w == null || expired(w, now)) {
            if (w == null && byIp.size() >= maxTrackedIps) {
                byIp.values().removeIf(x -> expired(x, now));
                if (byIp.size() >= maxTrackedIps) {
                    return false;   // troppi IP diversi in poco tempo: meglio rifiutare che esaurire la memoria
                }
            }
            w = new Window(now, 0);
        }
        if (w.count() >= perIp || globalWindow.count() >= global) {
            return false;
        }
        byIp.put(ip, new Window(w.start(), w.count() + 1));
        globalWindow = new Window(globalWindow.start(), globalWindow.count() + 1);
        return true;
    }

    private boolean expired(Window w, Instant now) {
        return !now.isBefore(w.start().plus(window));
    }
}
