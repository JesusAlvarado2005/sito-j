package dev.jj.web.site;

import dev.jj.web.client.ProfileDto;
import dev.jj.web.client.ServiceClients;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/** Copia statica per GitHub Pages: niente form, ma email e CV sempre raggiungibili. */
@SpringBootTest(properties = "site.static-mode=true")
@AutoConfigureMockMvc
class StaticModeTests {

    @Autowired MockMvc mvc;
    @MockitoBean ServiceClients clients;

    private void profileWithCv() {
        when(clients.profile(any())).thenReturn(Optional.of(new ProfileDto("Jeanfranco Alvarado", "me@example.test", "Milano", null,
                "Junior Backend Developer", "Bio", "/cv/cv.pdf", List.of(), List.of(), List.of(), List.of())));
        when(clients.projects(any())).thenReturn(Optional.empty());
    }

    @Test
    void niente_form_ma_email_e_cv() throws Exception {
        profileWithCv();
        mvc.perform(get("/it"))
                .andExpect(content().string(not(containsString("id=\"contactForm\""))))
                .andExpect(content().string(not(containsString("cf-turnstile"))))
                .andExpect(content().string(containsString("href=\"mailto:me@example.test\"")))
                .andExpect(content().string(containsString("href=\"/cv/cv.pdf\"")));
    }

    @Test
    void privacy_senza_form() throws Exception {
        profileWithCv();
        mvc.perform(get("/it/privacy"))
                .andExpect(content().string(containsString("non raccoglie dati personali")))
                .andExpect(content().string(containsString("un'email")))   // apostrofo non perso da MessageFormat
                .andExpect(content().string(not(containsString("Turnstile"))));
    }

    @Test
    void in_sottocartella_lingua_e_link_restano_giusti() throws Exception {
        profileWithCv();
        mvc.perform(get("/sito-j/it").contextPath("/sito-j"))
                .andExpect(content().string(containsString("<html lang=\"it\"")))
                .andExpect(content().string(containsString("Costruisco software lato server")))
                .andExpect(content().string(containsString("href=\"/sito-j/css/site.css\"")))
                .andExpect(content().string(containsString("href=\"/sito-j/cv/cv.pdf\"")))
                .andExpect(content().string(containsString("href=\"/sito-j/en\"")));
    }

    @Test
    void safeUrl_accetta_solo_link_sicuri() {
        assertThat(SiteService.safeUrl("/cv/cv.pdf")).isEqualTo("/cv/cv.pdf");
        assertThat(SiteService.safeUrl("https://github.com/x")).isEqualTo("https://github.com/x");
        assertThat(SiteService.safeUrl("mailto:a@b.it")).isEqualTo("mailto:a@b.it");
        assertThat(SiteService.safeUrl("//evil.example/x")).isNull();
        assertThat(SiteService.safeUrl("/\\evil.example")).isNull();
        assertThat(SiteService.safeUrl("javascript:alert(1)")).isNull();
        assertThat(SiteService.safeUrl(" JavaScript:alert(1)")).isNull();
    }
}
