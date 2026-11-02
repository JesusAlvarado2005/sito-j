package dev.jj.profile.profile;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repository di sola lettura per ora; ordinamento con Sort.by("sortOrder") nel service. */
interface ProfileRepository extends JpaRepository<Profile, Short> {
}

interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {
}

interface ExperienceRepository extends JpaRepository<Experience, Long> {
}

interface EducationRepository extends JpaRepository<Education, Long> {
}

interface SkillCategoryRepository extends JpaRepository<SkillCategory, Long> {
}
