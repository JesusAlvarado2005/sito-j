package dev.jj.web.client;

import java.time.LocalDate;
import java.util.List;

/** Risposta di projects-service: GET /api/v1/projects?lang= */
public record ProjectDto(
        String slug, String title, String summary, String context, String bodyMd, String coverImageUrl, String demoUrl,
        boolean featured, LocalDate startDate, LocalDate endDate, List<TagDto> tags, RepoDto repo) {

    public record TagDto(String name, String slug) {
    }

    public record RepoDto(String provider, String url) {
    }
}
