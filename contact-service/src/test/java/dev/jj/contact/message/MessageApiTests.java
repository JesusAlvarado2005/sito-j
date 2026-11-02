package dev.jj.contact.message;

import dev.jj.contact.TestcontainersConfiguration;
import dev.jj.contact.captcha.TurnstileVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class MessageApiTests {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean TurnstileVerifier turnstile;

    @BeforeEach
    void clean() {
        jdbc.update("DELETE FROM contact.contact_message");
    }

    private static String json(String name, String email, String body, String lang) {
        return """
                {"name": "%s", "email": "%s", "subject": "Hello", "body": "%s", "lang": "%s", "captchaToken": "tok"}
                """.formatted(name, email, body, lang);
    }

    private int count() {
        return jdbc.queryForObject("SELECT count(*) FROM contact.contact_message", Integer.class);
    }

    @Test
    void savesMessageAndStoresOnlyHashedIp() throws Exception {
        when(turnstile.verify(eq("tok"), eq("203.0.113.9"))).thenReturn(true);

        mvc.perform(post("/api/v1/messages").contentType(MediaType.APPLICATION_JSON)
                        .header("X-Client-Ip", "203.0.113.9")
                        .content(json("Mario", "mario@example.com", "Ciao!", "it")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        Map<String, Object> row = jdbc.queryForMap("SELECT * FROM contact.contact_message");
        assertThat(row.get("status")).isEqualTo("NEW");
        assertThat(row.get("notified_at")).isNull();
        assertThat(row.get("lang").toString().trim()).isEqualTo("it");
        assertThat((String) row.get("ip_hash")).hasSize(64).doesNotContain("203.0.113.9");
    }

    @Test
    void rejectsFailedCaptchaWithoutSaving() throws Exception {
        when(turnstile.verify(any(), any())).thenReturn(false);

        mvc.perform(post("/api/v1/messages").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Mario", "mario@example.com", "Ciao!", "it")))
                .andExpect(status().isBadRequest());
        assertThat(count()).isZero();
    }

    @Test
    void answers503WhenCaptchaProviderIsDown() throws Exception {
        when(turnstile.verify(any(), any())).thenThrow(new RestClientException("down"));

        mvc.perform(post("/api/v1/messages").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Mario", "mario@example.com", "Ciao!", "it")))
                .andExpect(status().isServiceUnavailable());
        assertThat(count()).isZero();
    }

    @Test
    void validatesInput() throws Exception {
        when(turnstile.verify(any(), any())).thenReturn(true);
        String tooLong = "x".repeat(5001);

        for (String body : new String[]{
                json("", "mario@example.com", "Ciao", "it"),            // nome vuoto
                json("Mario", "non-una-email", "Ciao", "it"),           // email non valida
                json("Mario", "mario@example.com", tooLong, "it"),      // corpo troppo lungo
                json("Mario", "mario@example.com", "Ciao", "fr")}) {    // lingua non supportata
            mvc.perform(post("/api/v1/messages").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        assertThat(count()).isZero();
    }

    private org.springframework.test.web.servlet.ResultActions send(String ip) throws Exception {
        var req = post("/api/v1/messages").contentType(MediaType.APPLICATION_JSON)
                .content(json("Mario", "mario@example.com", "Ciao!", "it"));
        return mvc.perform(ip == null ? req : req.header("X-Client-Ip", ip));
    }

    @Test
    void limitsMessagesPerIpWithoutCallingCloudflare() throws Exception {
        when(turnstile.verify(any(), any())).thenReturn(true);
        for (int i = 0; i < 5; i++) {
            send("198.51.100.1").andExpect(status().isCreated());
        }
        send("198.51.100.1").andExpect(status().isTooManyRequests());
        send("198.51.100.2").andExpect(status().isCreated());     // un altro IP passa
        verify(turnstile, org.mockito.Mockito.times(6)).verify(any(), any());  // il 6° non ha chiamato Cloudflare
    }

    @Test
    void appliesGlobalLimitAgainstRotatingIps() throws Exception {
        jdbc.update("""
                INSERT INTO contact.contact_message (id, name, email, subject, body, lang, ip_hash)
                SELECT gen_random_uuid(), 'x', 'x@example.com', 's', 'b', 'en', 'hash-' || g
                FROM generate_series(1, 50) g""");
        send("203.0.113.77").andExpect(status().isTooManyRequests());
        verify(turnstile, never()).verify(any(), any());
    }

    @Test
    void ignoresClientIpHeaderThatIsNotAnIp() throws Exception {
        when(turnstile.verify(any(), any())).thenReturn(true);
        send("<script>alert(1)</script>").andExpect(status().isCreated());
        verify(turnstile).verify(eq("tok"), isNull());             // a Cloudflare non arriva spazzatura
    }

    @Test
    void hashIsStableForSameIpAndDifferentAcrossIps() throws Exception {
        when(turnstile.verify(any(), any())).thenReturn(true);
        send("192.0.2.1");
        send("192.0.2.1");
        send("192.0.2.2");
        assertThat(jdbc.queryForObject("SELECT count(DISTINCT ip_hash) FROM contact.contact_message", Integer.class)).isEqualTo(2);
    }
}
