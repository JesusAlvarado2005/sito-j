package dev.jj.contact.captcha;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TurnstileVerifierTests {

    private static final String URL = "https://cf.test/verify";

    private TurnstileVerifier verifier(RestClient.Builder b, String secret) {
        return new TurnstileVerifier(b, secret, URL);
    }

    @Test
    void sendsSecretTokenAndIpAndAcceptsSuccess() {
        RestClient.Builder b = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(b).build();
        server.expect(requestTo(URL))
                .andExpect(content().string(containsString("secret=s3cret")))
                .andExpect(content().string(containsString("response=tok")))
                .andExpect(content().string(containsString("remoteip=1.2.3.4")))
                .andRespond(withSuccess("{\"success\": true}", APPLICATION_JSON));

        assertThat(verifier(b, "s3cret").verify("tok", "1.2.3.4")).isTrue();
        server.verify();
    }

    @Test
    void rejectsWhenCloudflareSaysNo() {
        RestClient.Builder b = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(b).build();
        server.expect(requestTo(URL)).andRespond(withSuccess("{\"success\": false, \"error-codes\": [\"invalid-input-response\"]}", APPLICATION_JSON));

        assertThat(verifier(b, "s3cret").verify("bad", null)).isFalse();
    }

    @Test
    void failsClosedWithoutSecretAndDoesNotCallCloudflare() {
        RestClient.Builder b = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(b).build();   // nessuna chiamata attesa

        assertThat(verifier(b, "").verify("tok", "1.2.3.4")).isFalse();
        server.verify();
    }

    @Test
    void propagatesNetworkErrors() {
        RestClient.Builder b = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(b).build();
        server.expect(requestTo(URL)).andRespond(withServerError());

        assertThatThrownBy(() -> verifier(b, "s3cret").verify("tok", null)).isInstanceOf(RestClientException.class);
    }
}
