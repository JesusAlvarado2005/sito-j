package dev.jj.web.contact;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ContactClientTests {

    MockRestServiceServer server;
    ContactClient client;
    ContactForm form = new ContactForm();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://contact");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ContactClient(builder.build());
        form.setName("Mario");
        form.setEmail("mario@example.test");
        form.setSubject("Ciao");
        form.setBody("Testo");
    }

    @Test
    void inviaIlJsonEMetteLIpDecisoDaWebPublic() {
        server.expect(requestTo("http://contact/api/v1/messages"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Client-Ip", "203.0.113.7"))
                .andExpect(jsonPath("$.name").value("Mario"))
                .andExpect(jsonPath("$.lang").value("it"))
                .andExpect(jsonPath("$.captchaToken").value("tok"))
                .andRespond(withStatus(HttpStatus.CREATED));
        assertThat(client.send(form, "it", "tok", "203.0.113.7")).isEqualTo(ContactClient.Result.SENT);
        server.verify();
    }

    @Test
    void traduceGliErroriDelServizio() {
        server.expect(requestTo("http://contact/api/v1/messages")).andRespond(withBadRequest());
        assertThat(client.send(form, "it", "tok", "1.1.1.1")).isEqualTo(ContactClient.Result.CAPTCHA_FAILED);
        server.reset();
        server.expect(requestTo("http://contact/api/v1/messages")).andRespond(withTooManyRequests());
        assertThat(client.send(form, "it", "tok", "1.1.1.1")).isEqualTo(ContactClient.Result.RATE_LIMITED);
        server.reset();
        server.expect(requestTo("http://contact/api/v1/messages")).andRespond(withServiceUnavailable());
        assertThat(client.send(form, "it", "tok", "1.1.1.1")).isEqualTo(ContactClient.Result.UNAVAILABLE);
    }

    @Test
    void servizioIrraggiungibileNonFaEsplodereLaPagina() {
        ContactClient unreachable = new ContactClient(RestClient.builder().baseUrl("http://127.0.0.1:1").build());
        assertThat(unreachable.send(form, "it", "tok", "1.1.1.1")).isEqualTo(ContactClient.Result.UNAVAILABLE);
    }
}
