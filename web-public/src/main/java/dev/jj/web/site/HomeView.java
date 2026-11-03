package dev.jj.web.site;

import java.util.List;

/** Tutto ciò che serve al template della home, già pronto (nessuna logica nel template). */
public record HomeView(
        String lang, String otherLang,
        String firstName, String lastName, String eyebrow,
        String bio, String avatarUrl, String email, String cvUrl,
        List<ProjectCard> featured, List<ProjectCard> projects,
        List<SkillRow> skillRows, SkillRow extraSkills,
        List<TimelineItem> timeline, List<Link> links,
        String seoTitle, String seoDescription, String canonicalUrl, String alternateUrl, String jsonLd) {

    public record ProjectCard(String slug, String title, String summary, String context, List<String> tags,
                              String repoUrl, String repoLabel, String demoUrl, List<Highlight> highlights) {
    }

    public record Highlight(String value, String label) {
    }

    public record SkillRow(String label, List<Chip> chips) {
    }

    /** icon = id del simbolo SVG, null se la skill non ha un logo. */
    public record Chip(String name, String icon) {
    }

    public record TimelineItem(String period, String title, String text) {
    }

    public record Link(String label, String url) {
    }
}
