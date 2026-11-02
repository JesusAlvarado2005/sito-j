package dev.jj.projects.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@Component
public class GitLabClient implements RepoClient {

    private final RestClient http;
    private final String baseUrl;

    public GitLabClient(RestClient.Builder builder,
                        @Value("${sync.gitlab.base-url:https://gitlab.com/api/v4}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.http = builder.defaultHeader("User-Agent", "sito-j-projects-service").build();
    }

    @Override
    public String provider() {
        return "GITLAB";
    }

    @Override
    public RepoInfo fetch(String path) {
        // GitLab vuole il path del progetto codificato: gruppo/progetto -> gruppo%2Fprogetto
        String project = baseUrl + "/projects/" + URLEncoder.encode(path, StandardCharsets.UTF_8);
        Repo repo = http.get().uri(URI.create(project)).retrieve().body(Repo.class);
        // GitLab restituisce già le percentuali per linguaggio
        Map<String, Double> languages = http.get().uri(URI.create(project + "/languages")).retrieve()
                .body(new ParameterizedTypeReference<Map<String, Double>>() {
                });
        Map<String, Double> sorted = new java.util.LinkedHashMap<>();
        if (languages != null) {
            languages.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .forEach(e -> sorted.put(e.getKey(), e.getValue()));
        }
        return new RepoInfo(repo.stars(), repo.forks(), RepoInfo.primaryOf(sorted), sorted,
                repo.lastActivityAt() == null ? null : Instant.parse(repo.lastActivityAt()));
    }

    private record Repo(@JsonProperty("star_count") int stars,
                        @JsonProperty("forks_count") int forks,
                        @JsonProperty("last_activity_at") String lastActivityAt) {
    }
}
