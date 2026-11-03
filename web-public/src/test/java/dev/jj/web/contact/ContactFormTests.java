package dev.jj.web.contact;

import dev.jj.web.client.ServiceClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"site.contact.per-ip=2", "site.contact.global=1000"})
@AutoConfigureMockMvc
class ContactFormTests {

    @Autowired MockMvc mvc;
    @MockitoBean ServiceClients services;
    @MockitoBean ContactClient contact;

    @BeforeEach
    void setUp() {
        when(services.profile(any())).thenReturn(Optional.empty());
        when(services.projects(any())).thenReturn(Optional.empty());
        when(contact.send(any(), any(), any(), any())).thenReturn(ContactClient.Result.SENT);
    }

    /** Ogni test usa un IP diverso: il rate limiter è condiviso nel contesto Spring. */
    private MockHttpServletRequestBuilder validPost(String ip) {
        return withoutToken(ip).param("cf-turnstile-response", "token-ok");
    }

    private MockHttpServletRequestBuilder withoutToken(String ip) {
        return post("/it/contact").with(r -> { r.setRemoteAddr(ip); return r; })
                .param("name", "Mario").param("email", "mario@example.test")
                .param("subject", "Ciao").param("body", "Vorrei parlarti di un ruolo.");
    }

    @Test
    void invioRiuscitoFaRedirectSenzaReinvioAlRefresh() throws Exception {
        mvc.perform(validPost("198.51.100.1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/it?sent#contatti"));
        verify(contact).send(argThat(f -> f.getName().equals("Mario")), eq("it"), eq("token-ok"), eq("198.51.100.1"));
    }

    @Test
    void lIpMandatoDalBrowserNonArrivaMaiAlServizio() throws Exception {
        mvc.perform(validPost("198.51.100.2").header("X-Client-Ip", "6.6.6.6")).andExpect(status().isFound());
        verify(contact).send(any(), any(), any(), eq("198.51.100.2"));
    }

    @Test
    void ilBotCheCompilaLaTrappolaVieneScartatoInSilenzio() throws Exception {
        mvc.perform(validPost("198.51.100.3").param("website", "http://spam.example"))
                .andExpect(redirectedUrl("/it?sent#contatti"));
        verifyNoInteractions(contact);
    }

    @Test
    void campiNonValidiRipresentanoIlFormSenzaChiamareIlServizio() throws Exception {
        mvc.perform(validPost("198.51.100.4").param("email", "non-una-email").param("body", ""))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().string(containsString("Controlla i campi evidenziati")))
                .andExpect(content().string(containsString("value=\"Mario\"")));   // i dati inseriti restano
        verifyNoInteractions(contact);
    }

    @Test
    void senzaTokenAntiBotNonSiChiamaIlServizio() throws Exception {
        mvc.perform(withoutToken("198.51.100.5"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(contact);
    }

    @Test
    void oltreIlLimitePerIpRisponde429() throws Exception {
        mvc.perform(validPost("198.51.100.6")).andExpect(status().isFound());
        mvc.perform(validPost("198.51.100.6")).andExpect(status().isFound());
        mvc.perform(validPost("198.51.100.6"))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string(containsString("troppi messaggi")));
        verify(contact, times(2)).send(any(), any(), any(), any());
    }

    @Test
    void serviceGiuMostraLAlternativaEmail() throws Exception {
        when(contact.send(any(), any(), any(), any())).thenReturn(ContactClient.Result.UNAVAILABLE);
        mvc.perform(validPost("198.51.100.7"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(containsString("mailto:")));
    }

    @Test
    void ilTestoInseritoVieneEscapato() throws Exception {
        mvc.perform(validPost("198.51.100.8").param("email", "x").param("name", "<script>alert(1)</script>"))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }

    @Test
    void dopoLInvioLaHomeMostraLaConferma() throws Exception {
        mvc.perform(get("/it").param("sent", ""))
                .andExpect(content().string(containsString("Messaggio inviato")))
                .andExpect(content().string(not(containsString("id=\"contactForm\""))));
    }

    @Test
    void laHomeContieneIlFormConLaSiteKey() throws Exception {
        mvc.perform(get("/en"))
                .andExpect(content().string(containsString("id=\"contactForm\"")))
                .andExpect(content().string(containsString("data-sitekey=\"1x00000000000000000000AA\"")))
                .andExpect(content().string(containsString("action=\"/en/contact\"")));
    }

    @Test
    void privacyInEntrambeLeLingue() throws Exception {
        mvc.perform(get("/it/privacy")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Come tratto i tuoi dati")))
                .andExpect(content().string(containsString("Dell'indirizzo IP")))   // apostrofo non perso da MessageFormat
                .andExpect(content().string(containsString("mailto:jean.alv0705@gmail.com")));
        mvc.perform(get("/en/privacy")).andExpect(status().isOk())
                .andExpect(content().string(containsString("How I handle your data")));
    }

    @Test
    void getSulContattoPortaAllaSezione() throws Exception {
        mvc.perform(get("/it/contact")).andExpect(redirectedUrl("/it#contatti"));
    }
}
