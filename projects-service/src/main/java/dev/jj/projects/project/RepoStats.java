package dev.jj.projects.project;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/** Statistiche del repository: l'id coincide con quello del progetto. */
@Entity
@Table(name = "repo_stats")
class RepoStats {

    @Id
    Long projectId;
    int stars;
    int forks;
    String primaryLanguage;

    @JdbcTypeCode(SqlTypes.JSON)
    Map<String, Double> languages;
    Instant lastCommitAt;
    Instant syncedAt;
    String syncError;
}
