package dev.jj.web.client;

import dev.jj.web.WebProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

/**
 * Client verso i microservizi interni. I timeout sono in application.yml (spring.http.clients.*).
 * Le risposte restano in cache; se un servizio è giù si usa l'ultimo dato disponibile.
 */
@Component
public class ServiceClients {

    private final RestClient profile;
    private final RestClient projects;
    private final StaleCache<String, ProfileDto> profileCache;
    private final StaleCache<String, List<ProjectDto>> projectsCache;

    public ServiceClients(RestClient.Builder builder, WebProperties props) {
        this.profile = builder.clone().baseUrl(props.profileServiceUrl()).build();
        this.projects = builder.clone().baseUrl(props.projectsServiceUrl()).build();
        this.profileCache = new StaleCache<>(props.cacheTtl());
        this.projectsCache = new StaleCache<>(props.cacheTtl());
    }

    public Optional<ProfileDto> profile(String lang) {
        return profileCache.get(lang, () -> profile.get()
                .uri("/api/v1/profile?lang={lang}", lang)
                .retrieve().body(ProfileDto.class));
    }

    public Optional<List<ProjectDto>> projects(String lang) {
        return projectsCache.get(lang, () -> projects.get()
                .uri("/api/v1/projects?lang={lang}", lang)
                .retrieve().body(new ParameterizedTypeReference<List<ProjectDto>>() {
                }));
    }
}
