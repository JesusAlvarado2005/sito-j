package dev.jj.projects.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByStatusOrderBySortOrderAscIdAsc(ProjectStatus status);

    Optional<Project> findBySlugAndStatus(String slug, ProjectStatus status);
}

interface RepoStatsRepository extends JpaRepository<RepoStats, Long> {
}
