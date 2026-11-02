package dev.jj.projects.project;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Sincronizza i repository dopo 1 minuto dall'avvio e poi ogni 6 ore. Disattivabile con sync.enabled=false. */
@Component
@EnableScheduling
@ConditionalOnProperty(name = "sync.enabled", havingValue = "true", matchIfMissing = true)
class RepoSyncJob {

    private final RepoSyncService service;

    RepoSyncJob(RepoSyncService service) {
        this.service = service;
    }

    @Scheduled(initialDelayString = "${sync.initial-delay:PT1M}", fixedDelayString = "${sync.interval:PT6H}")
    void run() {
        service.syncAll();
    }
}
