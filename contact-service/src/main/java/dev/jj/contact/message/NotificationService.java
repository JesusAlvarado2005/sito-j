package dev.jj.contact.message;

import dev.jj.contact.ContactProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Pattern "outbox": il messaggio è già nel DB; questo servizio invia le email in sospeso.
 * Se l'invio fallisce si riprova con backoff esponenziale (1, 2, 4, ... minuti, massimo 6 ore)
 * fino a max-attempts; poi si smette e il messaggio resta comunque leggibile nell'inbox admin.
 * Non è @Transactional: l'invio è lento e non deve tenere aperta una transazione.
 */
@Service
class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final int BATCH_SIZE = 20;
    private static final Duration MAX_BACKOFF = Duration.ofHours(6);

    private final MessageRepository messages;
    private final Notifier notifier;
    private final int maxAttempts;

    NotificationService(MessageRepository messages, Notifier notifier, ContactProperties props) {
        this.messages = messages;
        this.notifier = notifier;
        this.maxAttempts = props.notification().maxAttempts();
    }

    /** @return quanti messaggi sono stati notificati */
    int sendPending() {
        Instant now = Instant.now();
        int sent = 0;
        for (ContactMessage m : messages.findDueForNotification(maxAttempts, now, Limit.of(BATCH_SIZE))) {
            try {
                notifier.send(m);
                m.notifiedAt = Instant.now();
                m.nextAttemptAt = null;
                sent++;
            } catch (RuntimeException e) {
                m.notifyAttempts++;
                m.nextAttemptAt = now.plus(backoff(m.notifyAttempts));
                if (m.notifyAttempts >= maxAttempts) {
                    log.error("Notifica abbandonata per il messaggio {} dopo {} tentativi: {}",
                            m.id, m.notifyAttempts, e.getMessage());
                } else {
                    log.warn("Notifica fallita per il messaggio {} (tentativo {}): {}",
                            m.id, m.notifyAttempts, e.getMessage());
                }
            }
            messages.save(m);
        }
        return sent;
    }

    static Duration backoff(int attempts) {
        Duration d = Duration.ofMinutes(1L << Math.min(attempts - 1, 20));
        return d.compareTo(MAX_BACKOFF) > 0 ? MAX_BACKOFF : d;
    }
}
