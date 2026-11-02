package dev.jj.contact.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Pattern "outbox": il messaggio è già salvato nel DB e un job invia le email dei messaggi
 * con notified_at NULL. Se SMTP è giù il messaggio non si perde: si riprova al giro successivo.
 * Non è @Transactional: l'invio è lento e non deve tenere aperta una transazione.
 */
@Service
class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final int BATCH_SIZE = 20;

    private final MessageRepository messages;
    private final Notifier notifier;

    NotificationService(MessageRepository messages, Notifier notifier) {
        this.messages = messages;
        this.notifier = notifier;
    }

    /** @return quanti messaggi sono stati notificati */
    int sendPending() {
        int sent = 0;
        for (ContactMessage m : messages.findByNotifiedAtIsNullOrderByCreatedAtAsc(Limit.of(BATCH_SIZE))) {
            try {
                notifier.send(m);
                m.notifiedAt = Instant.now();
                messages.save(m);
                sent++;
            } catch (RuntimeException e) {
                log.warn("Notifica fallita per il messaggio {}: {}", m.id, e.getMessage());
            }
        }
        return sent;
    }
}
