package dev.jj.contact.message;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "contact.notification.enabled", havingValue = "true", matchIfMissing = true)
class NotificationJob {

    private final NotificationService service;

    NotificationJob(NotificationService service) {
        this.service = service;
    }

    @Scheduled(initialDelay = 10_000, fixedDelayString = "${contact.notification.interval:PT1M}")
    void run() {
        service.sendPending();
    }
}
