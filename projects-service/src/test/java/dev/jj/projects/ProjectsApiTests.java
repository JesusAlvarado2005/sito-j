package dev.jj.projects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ProjectsApiTests {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM projects.project");
        jdbc.update("DELETE FROM projects.tag");
        // 1: pubblicato, in evidenza, GitHub con statistiche, tag Java
        jdbc.update("INSERT INTO projects.tag (id, name, slug) VALUES (1, 'Java', 'java'), (2, 'Docker', 'docker')");
        jdbc.update("""
                INSERT INTO projects.project (id, slug, status, featured, repo_provider, repo_path, sort_order)
                VALUES (1, 'sito-j', 'PUBLISHED', true, 'GITHUB', 'JesusAlvarado2005/sito-j', 1),
                       (2, 'altro', 'PUBLISHED', false, 'NONE', null, 2),
                       (3, 'bozza', 'DRAFT', false, 'NONE', null, 3)""");
        jdbc.update("""
                INSERT INTO projects.project_translation (project_id, lang, title, summary, body_md) VALUES
                (1, 'en', 'Personal site', 'en summary', 'en body'),
                (1, 'it', 'Sito personale', 'riassunto it', 'corpo it'),
                (2, 'en', 'Other', 'only english', 'x'),
                (3, 'en', 'Draft', 'hidden', 'x')""");
        jdbc.update("INSERT INTO projects.project_tag VALUES (1, 1), (1, 2), (2, 2), (3, 1)");
        jdbc.update("""
                INSERT INTO projects.repo_stats (project_id, stars, forks, primary_language, languages, last_commit_at)
                VALUES (1, 7, 1, 'Java', '{"Java": 90.5, "HTML": 9.5}', '2026-10-20T14:00:00Z')""");
    }

    @Test
    void listsOnlyPublishedProjectsInRequestedLanguage() throws Exception {
        mvc.perform(get("/api/v1/projects").param("lang", "it"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].slug").value("sito-j"))
                .andExpect(jsonPath("$[0].title").value("Sito personale"))
                .andExpect(jsonPath("$[0].bodyMd").doesNotExist())
                .andExpect(jsonPath("$[0].repo.url").value("https://github.com/JesusAlvarado2005/sito-j"))
                .andExpect(jsonPath("$[0].repo.stars").value(7))
                .andExpect(jsonPath("$[0].repo.languages.Java").value(90.5))
                .andExpect(jsonPath("$[1].title").value("Other"))      // fallback EN
                .andExpect(jsonPath("$[1].repo").doesNotExist());
    }

    @Test
    void filtersByTagAndFeatured() throws Exception {
        mvc.perform(get("/api/v1/projects").param("tag", "docker"))
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/v1/projects").param("tag", "java"))
                .andExpect(jsonPath("$.length()").value(1))          // il progetto 3 è DRAFT
                .andExpect(jsonPath("$[0].slug").value("sito-j"));
        mvc.perform(get("/api/v1/projects").param("featured", "true"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void returnsDetailWithBody() throws Exception {
        mvc.perform(get("/api/v1/projects/sito-j").param("lang", "it"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bodyMd").value("corpo it"))
                .andExpect(jsonPath("$.tags.length()").value(2));
    }

    @Test
    void draftAndUnknownProjectsAreNotFound() throws Exception {
        mvc.perform(get("/api/v1/projects/bozza")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/projects/non-esiste")).andExpect(status().isNotFound());
    }

    @Test
    void listsOnlyTagsOfPublishedProjects() throws Exception {
        mvc.perform(get("/api/v1/tags"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Docker"));
    }

    @Test
    void rejectsUnsupportedLanguage() throws Exception {
        mvc.perform(get("/api/v1/projects").param("lang", "fr")).andExpect(status().isBadRequest());
    }
}
