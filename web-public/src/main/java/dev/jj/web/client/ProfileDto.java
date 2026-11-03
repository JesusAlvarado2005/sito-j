package dev.jj.web.client;

import java.time.LocalDate;
import java.util.List;

/** Risposta di profile-service: GET /api/v1/profile?lang= (stessi campi del DTO del servizio). */
public record ProfileDto(
        String fullName, String publicEmail, String location, String avatarUrl,
        String headline, String bioMd, String cvUrl,
        List<LinkDto> links, List<ExperienceDto> experiences,
        List<EducationDto> educations, List<SkillCategoryDto> skills) {

    public record LinkDto(String type, String url, String label) {
    }

    public record ExperienceDto(String company, String companyUrl, String location,
                                LocalDate startDate, LocalDate endDate, String role, String descriptionMd) {
    }

    public record EducationDto(String institution, LocalDate startDate, LocalDate endDate,
                               String degree, String descriptionMd) {
    }

    public record SkillDto(String name, Short level) {
    }

    public record SkillCategoryDto(String code, String name, List<SkillDto> skills) {
    }
}
