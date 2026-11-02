package dev.jj.projects.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

@Component
public class GitHubClient implements RepoClient {

    private final RestClient http;

    public GitHubClient(RestClient.Builder builder,
                        @Value("${sync.github.base-url:https://api.github.com}") String baseUrl,
                        @Value("${sync.github.token:}") String token) {
        RestClient.Builder b = builder.baseUrl(baseUrl)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("User-Agent", "sito-j-projects-service");
        if (StringUtils.hasText(token)) {   // senza token: 60 richieste/ora, sufficienti per la sync ogni 6 ore
            b.defaultHeader("Authorization", "Bearer " + token);
        }
        this.http = b.build();
    }

    @Override
    public String provider() {
        return "GITHUB";
    }

    @Override
    public RepoInfo fetch(String path) {
        Repo repo = http.get().uri("/repos/" + path).retrieve().body(Repo.class);
        Map<String, Long> bytes = http.get().uri("/repos/" + path + "/languages").retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        Map<String, Double> languages = RepoInfo.toPercentages(bytes == null ? Map.of() : bytes);
        return new RepoInfo(repo.stars(), repo.forks(), RepoInfo.primaryOf(languages), languages,
                repo.pushedAt() == null ? null : Instant.parse(repo.pushedAt()));
    }

    private record Repo(@JsonProperty("stargazers_count") int stars,
                        @JsonProperty("forks_count") int forks,
                        @JsonProperty("pushed_at") String pushedAt) {
    }
}
