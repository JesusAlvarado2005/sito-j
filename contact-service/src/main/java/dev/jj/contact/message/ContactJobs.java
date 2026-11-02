package dev.jj.contact.message;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Job pianificati del servizio. Disattivabili con contact.jobs.enabled=false (lo fanno i test). */
@Component
@EnableScheduling
@ConditionalOnProperty(name = "contact.jobs.enabled", havingValue = "true", matchIfMissing = true)
class ContactJobs {

    private final NotificationService notifications;
    private final RetentionService retention;

    ContactJobs(NotificationService notifications, RetentionService retention) {
        this.notifications = notifications;
        this.retention = retention;
    }

    @Scheduled(initialDelay = 10_000, fixedDelayString = "${contact.notification.interval:PT1M}")
    void sendNotifications() {
        notifications.sendPending();
    }

    /** Ogni notte alle 03:30. */
    @Scheduled(cron = "0 30 3 * * *")
    void purgeExpiredMessages() {
        retention.purgeExpired();
    }
}
