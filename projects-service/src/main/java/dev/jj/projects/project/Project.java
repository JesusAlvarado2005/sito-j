package dev.jj.projects.project;

import jakarta.persistence.*;
import org.hibernate.annotations.MapKeyJdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "project")
class Project {

    @Id
    Long id;
    String slug;

    @Enumerated(EnumType.STRING)
    ProjectStatus status;
    boolean featured;
    LocalDate startDate;
    LocalDate endDate;
    String repoProvider;   // GITHUB | GITLAB | NONE
    String repoPath;       // owner/nome
    String demoUrl;
    String coverImageUrl;
    int sortOrder;

    @ElementCollection
    @CollectionTable(name = "project_translation", joinColumns = @JoinColumn(name = "project_id"))
    @MapKeyColumn(name = "lang", length = 2)
    @MapKeyJdbcTypeCode(SqlTypes.CHAR)
    Map<String, Text> translations = new HashMap<>();

    @ManyToMany
    @JoinTable(name = "project_tag",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    Set<Tag> tags = new HashSet<>();

    @Embeddable
    record Text(String title, String summary, String bodyMd) {
    }
}
