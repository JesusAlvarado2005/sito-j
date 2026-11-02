package dev.jj.projects.project;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Scheda progetto. Nella lista bodyMd è null (e non viene serializzato); nel dettaglio è valorizzato. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProjectDto(
        String slug, String title, String summary, String bodyMd, String coverImageUrl, String demoUrl,
        boolean featured, LocalDate startDate, LocalDate endDate,
        List<TagDto> tags, RepoDto repo) {

    public record TagDto(String name, String slug) {
    }

    public record RepoDto(String provider, String url, Integer stars, Integer forks, String primaryLanguage,
                          Map<String, Double> languages, Instant lastCommitAt) {
    }
}
