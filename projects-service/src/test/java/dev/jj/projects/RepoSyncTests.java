package dev.jj.projects;

import dev.jj.projects.sync.GitHubClient;
import dev.jj.projects.sync.RepoInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/** Il job è package-private: lo si invoca via reflection, come farebbe lo scheduler. */
@SpringBootTest(properties = "sync.enabled=true")
@Import(TestcontainersConfiguration.class)
class RepoSyncTests {

    @Autowired JdbcTemplate jdbc;
    @Autowired org.springframework.context.ApplicationContext ctx;
    @MockitoBean GitHubClient gitHub;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM projects.project");
        jdbc.update("INSERT INTO projects.project (id, slug, status, repo_provider, repo_path) VALUES (1, 'a', 'PUBLISHED', 'GITHUB', 'me/a')");
        jdbc.update("INSERT INTO projects.project (id, slug, status, repo_provider, repo_path) VALUES (2, 'b', 'PUBLISHED', 'NONE', null)");
        when(gitHub.provider()).thenReturn("GITHUB");
    }

    private void runSync() throws Exception {
        Object job = ctx.getBean("repoSyncJob");
        var run = job.getClass().getDeclaredMethod("run");
        run.setAccessible(true);
        run.invoke(job);
    }

    @Test
    void savesStatsFromProvider() throws Exception {
        when(gitHub.fetch(anyString())).thenReturn(
                new RepoInfo(9, 1, "Java", Map.of("Java", 100.0), Instant.parse("2026-10-20T14:00:00Z")));

        runSync();

        assertThat(jdbc.queryForObject("SELECT stars FROM projects.repo_stats WHERE project_id = 1", Integer.class)).isEqualTo(9);
        assertThat(jdbc.queryForObject("SELECT primary_language FROM projects.repo_stats WHERE project_id = 1", String.class)).isEqualTo("Java");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM projects.repo_stats", Integer.class)).isEqualTo(1);  // il progetto senza repo è ignorato
    }

    @Test
    void keepsOldStatsAndStoresErrorWhenProviderFails() throws Exception {
        jdbc.update("INSERT INTO projects.repo_stats (project_id, stars) VALUES (1, 5)");
        when(gitHub.fetch(anyString())).thenThrow(new IllegalStateException("404 Not Found"));

        runSync();

        assertThat(jdbc.queryForObject("SELECT stars FROM projects.repo_stats WHERE project_id = 1", Integer.class)).isEqualTo(5);
        assertThat(jdbc.queryForObject("SELECT sync_error FROM projects.repo_stats WHERE project_id = 1", String.class)).contains("404");
    }
}
