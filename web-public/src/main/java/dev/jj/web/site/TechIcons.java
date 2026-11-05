package dev.jj.web.site;

import java.util.Map;

/** Nome della skill (come nel DB) -> id del simbolo SVG in templates/fragments.html. Senza logo: niente icona. */
final class TechIcons {

    private static final Map<String, String> ICONS = Map.ofEntries(
            Map.entry("Java", "openjdk"), Map.entry("Go", "go"), Map.entry("Spring Boot", "springboot"),
            Map.entry("Hibernate", "hibernate"), Map.entry("Gin", "gin"), Map.entry("PostgreSQL", "postgresql"),
            Map.entry("MySQL", "mysql"), Map.entry("InfluxDB", "influxdb"),
            Map.entry("Vaadin", "vaadin"), Map.entry("JUnit 5", "junit5"), Map.entry("Maven", "apachemaven"),
            Map.entry("Swagger", "swagger"), Map.entry("JWT", "jsonwebtokens"), Map.entry("Python", "python"),
            Map.entry("Azure", "azure"), Map.entry("Azure DevOps", "azuredevops"), Map.entry("Terraform", "terraform"),
            Map.entry("Docker", "docker"), Map.entry("Kubernetes", "kubernetes"),
            Map.entry("GitHub Actions", "githubactions"), Map.entry("Linux", "linux"),
            Map.entry("Proxmox VE", "proxmox"), Map.entry("LXC", "linuxcontainers"), Map.entry("Grafana", "grafana"),
            Map.entry("Gitea", "gitea"), Map.entry("MQTT", "mqtt"), Map.entry("Git", "git"),
            Map.entry("Claude Code", "claude"));

    private TechIcons() {
    }

    static String idFor(String skillName) {
        return ICONS.get(skillName);
    }
}
