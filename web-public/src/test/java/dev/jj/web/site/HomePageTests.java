package dev.jj.web.site;

import dev.jj.web.client.ProfileDto;
import dev.jj.web.client.ProfileDto.*;
import dev.jj.web.client.ProjectDto;
import dev.jj.web.client.ServiceClients;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HomePageTests {

    @Autowired MockMvc mvc;
    @MockitoBean ServiceClients clients;

    private ProfileDto profile(String lang) {
        return new ProfileDto("Jeanfranco Alvarado", "me@example.test", "Milano", null,
                lang.equals("it") ? "Junior Backend Developer · Milano" : "Junior Backend Developer · Milan",
                lang.equals("it") ? "Bio italiana" : "English bio", null,
                List.of(new LinkDto("GITHUB", "https://github.com/x", "GitHub"), new LinkDto("OTHER", "javascript:alert(1)", "Cattivo")),
                List.of(new ExperienceDto("Sysnet", null, null, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 7, 31), "Tirocinante", "Go")),
                List.of(new EducationDto("ITS", LocalDate.of(2024, 10, 1), null, "Diploma", null)),
                List.of(new SkillCategoryDto("BACKEND", "Backend", List.of(new SkillDto("Java", null), new SkillDto("Nonesiste", null))),
                        new SkillCategoryDto("OTHER", "E poi", List.of(new SkillDto("API REST", null)))));
    }

    private ProjectDto project(String title, boolean featured) {
        return new ProjectDto("p", title, "Sommario", "Contesto", null, null, null, featured, null, null,
                List.of(new ProjectDto.TagDto("Java", "java")), new ProjectDto.RepoDto("GITLAB", "https://gitlab.com/a/b"));
    }

    private void servicesUp() {
        when(clients.profile(any())).thenAnswer(i -> Optional.of(profile(i.getArgument(0))));
        when(clients.projects(any())).thenReturn(Optional.of(List.of(project("Progetto Uno", true), project("Progetto Due", false))));
    }

    @Test
    void homeItalianaConDatiDeiServizi() throws Exception {
        servicesUp();
        mvc.perform(get("/it"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<html lang=\"it\"")))
                .andExpect(content().string(containsString("Bio italiana")))
                .andExpect(content().string(containsString("Progetto Uno")))
                .andExpect(content().string(containsString("Costruisco software lato server")))
                .andExpect(content().string(containsString("Aprile 2026 – Luglio 2026")))   // mese in italiano, maiuscola
                .andExpect(content().string(containsString("2024 – in corso")))
                .andExpect(content().string(containsString("<link rel=\"canonical\" href=\"https://example.test/it\"")))
                .andExpect(content().string(containsString("href=\"https://example.test/en\" hreflang=\"en\"")))
                .andExpect(content().string(containsString("\"@type\":\"Person\"")));
    }

    @Test
    void homeInglese() throws Exception {
        servicesUp();
        mvc.perform(get("/en"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<html lang=\"en\"")))
                .andExpect(content().string(containsString("English bio")))
                .andExpect(content().string(containsString("I build server-side software")))
                .andExpect(content().string(containsString("April 2026 – July 2026")))
                .andExpect(content().string(containsString("2024 – present")));
    }

    @Test
    void ilLogoSiVedeSoloPerLeSkillCheLoHanno() throws Exception {
        servicesUp();
        mvc.perform(get("/it"))
                .andExpect(content().string(containsString("href=\"#i-openjdk\"")))
                .andExpect(content().string(containsString("Nonesiste")))
                .andExpect(content().string(containsString("API REST")));
    }

    @Test
    void seIServiziSonoGiuLaPaginaSiVedeComunque() throws Exception {
        when(clients.profile(any())).thenReturn(Optional.empty());
        when(clients.projects(any())).thenReturn(Optional.empty());
        mvc.perform(get("/it"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("I progetti non sono disponibili")))
                .andExpect(content().string(containsString("Jeanfranco")));
    }

    @Test
    void ilContenutoDeiServiziVieneEscapato() throws Exception {
        when(clients.profile(any())).thenReturn(Optional.empty());
        when(clients.projects(any())).thenReturn(Optional.of(List.of(project("<script>alert(1)</script>", false))));
        mvc.perform(get("/it"))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")));
    }

    @Test
    void unLinkJavascriptNelDbNonDiventaUnLink() throws Exception {
        servicesUp();
        mvc.perform(get("/it")).andExpect(content().string(not(containsString("javascript:"))));
    }

    @Test
    void ilJsonLdNonPuoChiudereIlTagScript() throws Exception {
        when(clients.profile(any())).thenReturn(Optional.of(new ProfileDto("A</script><b>", null, null, null, "h", null, null,
                List.of(), List.of(), List.of(), List.of())));
        when(clients.projects(any())).thenReturn(Optional.empty());
        mvc.perform(get("/it")).andExpect(content().string(not(containsString("A</script>"))));
    }

    @Test
    void laRadiceSceglieLaLinguaDalBrowser() throws Exception {
        mvc.perform(get("/").header("Accept-Language", "it-IT,it;q=0.9,en;q=0.8")).andExpect(redirectedUrl("/it"));
        mvc.perform(get("/").header("Accept-Language", "fr-FR,fr;q=0.9")).andExpect(redirectedUrl("/en"));
        mvc.perform(get("/")).andExpect(redirectedUrl("/en"));
        mvc.perform(get("/").header("Accept-Language", "!!non valido!!")).andExpect(redirectedUrl("/en"));
    }

    @Test
    void lingueSconosciuteDanno404() throws Exception {
        mvc.perform(get("/de")).andExpect(status().isNotFound());
    }

    @Test
    void robotsESitemap() throws Exception {
        mvc.perform(get("/robots.txt")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Sitemap: https://example.test/sitemap.xml")));
        mvc.perform(get("/sitemap.xml")).andExpect(status().isOk())
                .andExpect(content().string(containsString("<loc>https://example.test/it</loc>")))
                .andExpect(content().string(containsString("hreflang=\"en\"")));
    }
}
