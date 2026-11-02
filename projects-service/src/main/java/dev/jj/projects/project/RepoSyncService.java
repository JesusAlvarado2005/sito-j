package dev.jj.projects.project;

import dev.jj.projects.sync.RepoClient;
import dev.jj.projects.sync.RepoInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Aggiorna repo_stats leggendo da GitHub/GitLab. Volutamente NON @Transactional:
 * le chiamate HTTP sono lente e non devono tenere aperta una transazione sul DB;
 * ogni salvataggio è una piccola transazione a sé.
 */
@Service
class RepoSyncService {

    private static final Logger log = LoggerFactory.getLogger(RepoSyncService.class);

    private final ProjectRepository projects;
    private final RepoStatsRepository stats;
    private final List<RepoClient> clients;

    RepoSyncService(ProjectRepository projects, RepoStatsRepository stats, List<RepoClient> clients) {
        this.projects = projects;
        this.stats = stats;
        this.clients = clients;
    }

    void syncAll() {
        for (Project p : projects.findAll()) {
            if (!"NONE".equals(p.repoProvider)) {
                syncOne(p);   // un errore su un progetto non ferma gli altri
            }
        }
    }

    void syncOne(Project p) {
        RepoStats s = stats.findById(p.id).orElseGet(() -> {
            RepoStats created = new RepoStats();
            created.projectId = p.id;
            return created;
        });
        try {
            RepoClient client = clients.stream().filter(c -> p.repoProvider.equals(c.provider())).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Nessun client per il provider " + p.repoProvider));
            RepoInfo info = client.fetch(p.repoPath);
            s.stars = info.stars();
            s.forks = info.forks();
            s.primaryLanguage = info.primaryLanguage();
            s.languages = info.languages();
            s.lastCommitAt = info.lastCommitAt();
            s.syncError = null;
        } catch (RuntimeException e) {
            // si tengono i vecchi dati e si registra l'errore
            log.warn("Sync fallita per {} ({}): {}", p.slug, p.repoPath, e.getMessage());
            String msg = String.valueOf(e.getMessage());
            s.syncError = msg.length() > 500 ? msg.substring(0, 500) : msg;
        }
        s.syncedAt = Instant.now();
        stats.save(s);
    }
}
