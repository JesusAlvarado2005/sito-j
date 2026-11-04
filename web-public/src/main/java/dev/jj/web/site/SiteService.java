package dev.jj.web.site;

import dev.jj.web.WebProperties;
import dev.jj.web.client.ProfileDto;
import dev.jj.web.client.ProjectDto;
import dev.jj.web.client.ServiceClients;
import dev.jj.web.site.HomeView.*;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

/** Compone la home unendo i dati dei servizi (profilo, progetti) con i testi del sito (messages_*.properties). */
@Service
public class SiteService {

    private final ServiceClients clients;
    private final MessageSource messages;
    private final WebProperties props;
    private final JsonMapper json;

    public SiteService(ServiceClients clients, MessageSource messages, WebProperties props, JsonMapper json) {
        this.clients = clients;
        this.messages = messages;
        this.props = props;
        this.json = json;
    }

    public HomeView home(String lang) {
        Locale locale = Locale.forLanguageTag(lang);
        String other = lang.equals("it") ? "en" : "it";
        ProfileDto profile = clients.profile(lang).orElse(null);
        List<ProjectDto> projects = clients.projects(lang).orElse(List.of());

        String fullName = profile != null && notBlank(profile.fullName()) ? profile.fullName() : msg("site.owner", locale);
        int space = fullName.indexOf(' ');
        String first = space < 0 ? fullName : fullName.substring(0, space);
        String last = space < 0 ? "" : fullName.substring(space + 1);

        List<ProjectCard> cards = projects.stream().map(p -> card(p, locale)).toList();
        List<ProjectCard> featured = new ArrayList<>(), others = new ArrayList<>();
        for (int i = 0; i < projects.size(); i++) {
            (projects.get(i).featured() ? featured : others).add(cards.get(i));
        }

        String title = msg("seo.title", locale);
        String description = msg("seo.description", locale);
        String canonical = props.baseUrl() + "/" + lang;
        return new HomeView(lang, other, first, last,
                profile != null && notBlank(profile.headline()) ? profile.headline() : msg("hero.eyebrow", locale),
                profile == null ? null : profile.bioMd(),
                profile == null ? null : safeUrl(profile.avatarUrl()),
                profile != null && notBlank(profile.publicEmail()) ? profile.publicEmail() : msg("site.email", locale),
                profile == null ? null : safeUrl(profile.cvUrl()),
                featured, others,
                profile == null ? List.of() : skillRows(profile, "BACKEND", "CLOUD"),
                profile == null ? null : skillRows(profile, "OTHER").stream().findFirst().orElse(null),
                profile == null ? List.of() : timeline(profile, lang, locale),
                profile == null ? List.of() : links(profile),
                title, description, canonical, props.baseUrl() + "/" + other,
                jsonLd(fullName, profile, canonical, description));
    }

    private ProjectCard card(ProjectDto p, Locale locale) {
        String repoLabel = p.repo() == null ? null : switch (p.repo().provider()) {
            case "GITHUB" -> "GitHub";
            case "GITLAB" -> "GitLab";
            default -> null;
        };
        List<Highlight> highlights = new ArrayList<>();
        for (int n = 1; n <= 3; n++) {   // evidenze opzionali, definite nei testi: project.<slug>.hl<n>.value / .label
            String value = messages.getMessage("project." + p.slug() + ".hl" + n + ".value", null, null, locale);
            String label = messages.getMessage("project." + p.slug() + ".hl" + n + ".label", null, null, locale);
            if (value != null && label != null) {
                highlights.add(new Highlight(value, label));
            }
        }
        return new ProjectCard(p.title(), p.summary(), p.context(),
                p.tags() == null ? List.of() : p.tags().stream().map(ProjectDto.TagDto::name).toList(),
                p.repo() == null ? null : safeUrl(p.repo().url()), repoLabel, highlights);
    }

    private List<SkillRow> skillRows(ProfileDto profile, String... codes) {
        List<SkillRow> rows = new ArrayList<>();
        for (String code : codes) {
            profile.skills().stream().filter(c -> c.code().equals(code)).findFirst().ifPresent(c ->
                    rows.add(new SkillRow(c.name(), c.skills().stream()
                            .map(s -> new Chip(s.name(), TechIcons.idFor(s.name()))).toList())));
        }
        return rows;
    }

    /** Esperienze e formazione in un'unica timeline, dalla più recente. Le esperienze mostrano mese e anno, la formazione solo gli anni. */
    private List<TimelineItem> timeline(ProfileDto profile, String lang, Locale locale) {
        String present = msg("timeline.present", locale);
        DateTimeFormatter month = DateTimeFormatter.ofPattern("MMMM yyyy", locale);
        record Dated(LocalDate start, TimelineItem item) {
        }
        Stream<Dated> experiences = profile.experiences().stream().map(e -> new Dated(e.startDate(), new TimelineItem(
                capitalize(month.format(e.startDate())) + " – " + (e.endDate() == null ? present : capitalize(month.format(e.endDate()))),
                e.company() + " · " + e.role(), e.descriptionMd())));
        Stream<Dated> educations = profile.educations().stream().map(e -> new Dated(e.startDate(), new TimelineItem(
                years(e.startDate(), e.endDate(), present), e.institution(),
                e.degree() + (notBlank(e.descriptionMd()) ? ". " + e.descriptionMd() + "." : "."))));
        return Stream.concat(experiences, educations)
                .sorted(Comparator.comparing(Dated::start).reversed()).map(Dated::item).toList();
    }

    private static String years(LocalDate start, LocalDate end, String present) {
        if (end == null) {
            return start.getYear() + " – " + present;
        }
        return start.getYear() == end.getYear() ? String.valueOf(start.getYear()) : start.getYear() + " – " + end.getYear();
    }

    private List<Link> links(ProfileDto profile) {
        return profile.links().stream()
                .filter(l -> safeUrl(l.url()) != null)
                .map(l -> new Link(notBlank(l.label()) ? l.label() : l.type(), l.url())).toList();
    }

    /** JSON-LD "Person" per Google. Il "<" è escapato così il JSON non può chiudere il tag <script>. */
    private String jsonLd(String name, ProfileDto profile, String url, String description) {
        Map<String, Object> person = new LinkedHashMap<>();
        person.put("@context", "https://schema.org");
        person.put("@type", "Person");
        person.put("name", name);
        person.put("url", url);
        person.put("description", description);
        if (profile != null) {
            if (notBlank(profile.headline())) {
                person.put("jobTitle", profile.headline());
            }
            if (notBlank(profile.location())) {
                person.put("address", Map.of("@type", "PostalAddress", "addressLocality", profile.location()));
            }
            person.put("sameAs", profile.links().stream().map(ProfileDto.LinkDto::url)
                    .filter(u -> u != null && u.startsWith("https://")).toList());
        }
        return json.writeValueAsString(person).replace("<", "\\u003c");
    }

    /**
     * Ammette solo http(s), mailto e percorsi interni al sito ("/cv/file.pdf"):
     * un "javascript:" salvato per errore nel DB non diventa mai un link.
     * "//host" e "/\host" sono esclusi: il browser li tratterebbe come un sito esterno.
     */
    static String safeUrl(String url) {
        if (url == null) {
            return null;
        }
        String trimmed = url.trim();
        String u = trimmed.toLowerCase(Locale.ROOT);
        boolean sitePath = u.startsWith("/") && !u.startsWith("//") && !u.startsWith("/\\");
        return sitePath || u.startsWith("https://") || u.startsWith("http://") || u.startsWith("mailto:") ? trimmed : null;
    }

    private String msg(String code, Locale locale) {
        return messages.getMessage(code, null, locale);
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
