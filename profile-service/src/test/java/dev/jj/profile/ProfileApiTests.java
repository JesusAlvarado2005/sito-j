package dev.jj.profile;

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
class ProfileApiTests {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM profile.profile");
        jdbc.update("DELETE FROM profile.social_link");
        jdbc.update("DELETE FROM profile.experience");
        jdbc.update("DELETE FROM profile.education");
        jdbc.update("DELETE FROM profile.skill_category");
    }

    private void seedFull() {
        jdbc.update("INSERT INTO profile.profile (id, full_name) VALUES (1, 'Jesus Alvarado')");
        jdbc.update("INSERT INTO profile.profile_translation VALUES (1, 'en', 'Junior Java Developer', 'bio en', null)");
        jdbc.update("INSERT INTO profile.profile_translation VALUES (1, 'it', 'Sviluppatore Java junior', 'bio it', null)");
        jdbc.update("INSERT INTO profile.social_link (type, url, sort_order) VALUES ('GITHUB', 'https://github.com/JesusAlvarado2005', 1)");
        jdbc.update("INSERT INTO profile.experience (id, company, start_date, sort_order) VALUES (10, 'ACME', '2025-01-01', 1)");
        jdbc.update("INSERT INTO profile.experience_translation VALUES (10, 'en', 'Intern', null)");
        jdbc.update("INSERT INTO profile.skill_category (id, code, sort_order) VALUES (20, 'BACKEND', 1)");
        jdbc.update("INSERT INTO profile.skill_category_translation VALUES (20, 'en', 'Backend')");
        jdbc.update("INSERT INTO profile.skill (category_id, name, level, sort_order) VALUES (20, 'Spring Boot', 3, 1)");
    }

    @Test
    void returnsItalianTexts() throws Exception {
        seedFull();
        mvc.perform(get("/api/v1/profile").param("lang", "it"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Jesus Alvarado"))
                .andExpect(jsonPath("$.headline").value("Sviluppatore Java junior"))
                .andExpect(jsonPath("$.links[0].type").value("GITHUB"))
                .andExpect(jsonPath("$.skills[0].skills[0].name").value("Spring Boot"));
    }

    @Test
    void fallsBackToEnglishWhenTranslationMissing() throws Exception {
        seedFull();
        // esperienza e categoria skill hanno solo la traduzione EN
        mvc.perform(get("/api/v1/profile").param("lang", "it"))
                .andExpect(jsonPath("$.experiences[0].role").value("Intern"))
                .andExpect(jsonPath("$.skills[0].name").value("Backend"));
    }

    @Test
    void defaultsToEnglish() throws Exception {
        seedFull();
        mvc.perform(get("/api/v1/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Junior Java Developer"));
    }

    @Test
    void rejectsUnsupportedLanguage() throws Exception {
        mvc.perform(get("/api/v1/profile").param("lang", "fr"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns404WhenProfileNotConfigured() throws Exception {
        mvc.perform(get("/api/v1/profile"))
                .andExpect(status().isNotFound());
    }
}
