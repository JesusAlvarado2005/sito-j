package dev.jj.projects.sync;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Test senza Spring né rete: le risposte di GitHub/GitLab sono simulate. */
class RepoClientsTests {

    @Test
    void gitHubClientReadsStatsAndConvertsBytesToPercentages() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GitHubClient client = new GitHubClient(builder, "https://gh.test", "secret");

        server.expect(requestTo("https://gh.test/repos/me/app"))
                .andExpect(method(GET)).andExpect(header("Authorization", "Bearer secret"))
                .andRespond(withSuccess("""
                        {"stargazers_count": 7, "forks_count": 2, "pushed_at": "2026-10-20T14:00:00Z", "name": "app"}
                        """, APPLICATION_JSON));
        server.expect(requestTo("https://gh.test/repos/me/app/languages"))
                .andRespond(withSuccess("{\"HTML\": 200, \"Java\": 800}", APPLICATION_JSON));

        RepoInfo info = client.fetch("me/app");

        assertThat(info.stars()).isEqualTo(7);
        assertThat(info.forks()).isEqualTo(2);
        assertThat(info.primaryLanguage()).isEqualTo("Java");
        assertThat(info.languages()).containsEntry("Java", 80.0).containsEntry("HTML", 20.0);
        assertThat(info.lastCommitAt()).hasToString("2026-10-20T14:00:00Z");
        server.verify();
    }

    @Test
    void gitHubClientFailsOnMissingRepository() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GitHubClient client = new GitHubClient(builder, "https://gh.test", "");
        server.expect(requestTo("https://gh.test/repos/me/none")).andRespond(withStatus(NOT_FOUND));

        assertThatThrownBy(() -> client.fetch("me/none")).isInstanceOf(RuntimeException.class);
    }

    @Test
    void gitLabClientEncodesProjectPath() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GitLabClient client = new GitLabClient(builder, "https://gl.test/api/v4");

        server.expect(requestTo("https://gl.test/api/v4/projects/group%2Fapp"))
                .andRespond(withSuccess("""
                        {"star_count": 3, "forks_count": 0, "last_activity_at": "2026-10-21T09:30:00.123Z"}
                        """, APPLICATION_JSON));
        server.expect(requestTo("https://gl.test/api/v4/projects/group%2Fapp/languages"))
                .andRespond(withSuccess("{\"Python\": 30.5, \"Java\": 69.5}", APPLICATION_JSON));

        RepoInfo info = client.fetch("group/app");

        assertThat(info.stars()).isEqualTo(3);
        assertThat(info.primaryLanguage()).isEqualTo("Java");
        server.verify();
    }
}
