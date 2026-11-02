package dev.jj.contact.message;

import dev.jj.contact.ContactProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/** GDPR: i dati personali non si tengono per sempre. Cancella i messaggi più vecchi di contact.retention. */
@Service
class RetentionService {

    private static final Logger log = LoggerFactory.getLogger(RetentionService.class);

    private final MessageRepository messages;
    private final Duration retention;

    RetentionService(MessageRepository messages, ContactProperties props) {
        this.messages = messages;
        this.retention = props.retention();
    }

    int purgeExpired() {
        int deleted = messages.deleteCreatedBefore(Instant.now().minus(retention));
        if (deleted > 0) {
            log.info("Retention: cancellati {} messaggi più vecchi di {}", deleted, retention);
        }
        return deleted;
    }
}
