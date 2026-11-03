package dev.jj.projects.project;

import dev.jj.projects.project.ProjectDto.RepoDto;
import dev.jj.projects.project.ProjectDto.TagDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
class ProjectService {

    private static final String FALLBACK_LANG = "en";

    private final ProjectRepository projects;
    private final RepoStatsRepository stats;

    ProjectService(ProjectRepository projects, RepoStatsRepository stats) {
        this.projects = projects;
        this.stats = stats;
    }

    /**
     * Elenco dei progetti PUBLISHED. I filtri sono applicati in memoria: per un portfolio
     * (poche decine di progetti) è più semplice di una query dinamica e abbastanza veloce.
     */
    @Transactional(readOnly = true)
    List<ProjectDto> list(String lang, String tagSlug, Boolean featured) {
        List<Project> published = projects.findByStatusOrderBySortOrderAscIdAsc(ProjectStatus.PUBLISHED).stream()
                .filter(p -> featured == null || p.featured == featured)
                .filter(p -> tagSlug == null || p.tags.stream().anyMatch(t -> t.slug.equals(tagSlug)))
                .toList();
        Map<Long, RepoStats> statsById = loadStats(published);
        return published.stream()
                .map(p -> toDto(p, lang, statsById.get(p.id), false))
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional(readOnly = true)
    ProjectDto detail(String slug, String lang) {
        Project p = projects.findBySlugAndStatus(slug, ProjectStatus.PUBLISHED)
                .orElseThrow(() -> notFound(slug));
        ProjectDto dto = toDto(p, lang, stats.findById(p.id).orElse(null), true);
        if (dto == null) {
            throw notFound(slug);
        }
        return dto;
    }

    /** Solo i tag usati da progetti pubblicati, ordinati per nome (per i filtri del sito). */
    @Transactional(readOnly = true)
    List<TagDto> usedTags() {
        return projects.findByStatusOrderBySortOrderAscIdAsc(ProjectStatus.PUBLISHED).stream()
                .flatMap(p -> p.tags.stream())
                .collect(Collectors.toMap(t -> t.slug, Function.identity(), (a, b) -> a))
                .values().stream()
                .sorted(Comparator.comparing(t -> t.name.toLowerCase()))
                .map(t -> new TagDto(t.name, t.slug))
                .toList();
    }

    private Map<Long, RepoStats> loadStats(List<Project> list) {
        return stats.findAllById(list.stream().map(p -> p.id).toList()).stream()
                .collect(Collectors.toMap(s -> s.projectId, Function.identity()));
    }

    /** null se il progetto non ha nessuna traduzione utilizzabile. */
    private ProjectDto toDto(Project p, String lang, RepoStats s, boolean withBody) {
        Project.Text t = p.translations.getOrDefault(lang, p.translations.get(FALLBACK_LANG));
        if (t == null) {
            return null;
        }
        List<TagDto> tags = p.tags.stream()
                .sorted(Comparator.comparing(tag -> tag.name.toLowerCase()))
                .map(tag -> new TagDto(tag.name, tag.slug)).toList();
        return new ProjectDto(p.slug, t.title(), t.summary(), t.context(), withBody ? t.bodyMd() : null,
                p.coverImageUrl, p.demoUrl, p.featured, p.startDate, p.endDate, tags, repo(p, s));
    }

    private static RepoDto repo(Project p, RepoStats s) {
        String base = switch (p.repoProvider) {
            case "GITHUB" -> "https://github.com/";
            case "GITLAB" -> "https://gitlab.com/";
            default -> null;
        };
        if (base == null) {
            return null;
        }
        return new RepoDto(p.repoProvider, base + p.repoPath,
                s == null ? null : s.stars, s == null ? null : s.forks,
                s == null ? null : s.primaryLanguage, s == null ? null : s.languages,
                s == null ? null : s.lastCommitAt);
    }

    private static ResponseStatusException notFound(String slug) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found: " + slug);
    }
}
