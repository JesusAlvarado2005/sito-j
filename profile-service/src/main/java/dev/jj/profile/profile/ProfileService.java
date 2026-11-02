package dev.jj.profile.profile;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
class ProfileService {

    private static final String FALLBACK_LANG = "en";
    private static final Sort BY_ORDER = Sort.by("sortOrder");
    private static final Short PROFILE_ID = 1;

    private final ProfileRepository profiles;
    private final SocialLinkRepository links;
    private final ExperienceRepository experiences;
    private final EducationRepository educations;
    private final SkillCategoryRepository categories;

    ProfileService(ProfileRepository profiles, SocialLinkRepository links, ExperienceRepository experiences,
                   EducationRepository educations, SkillCategoryRepository categories) {
        this.profiles = profiles;
        this.links = links;
        this.experiences = experiences;
        this.educations = educations;
        this.categories = categories;
    }

    /** @Transactional serve perché le traduzioni sono LAZY: si leggono finché la sessione è aperta. */
    @Transactional(readOnly = true)
    ProfileDto getProfile(String lang) {
        Profile p = profiles.findById(PROFILE_ID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not configured"));
        Profile.Text text = pick(p.translations, lang);

        return new ProfileDto(
                p.fullName, p.publicEmail, p.location, p.avatarUrl,
                text == null ? null : text.headline(),
                text == null ? null : text.bioMd(),
                text == null ? null : text.cvUrl(),
                links.findAll(BY_ORDER).stream()
                        .map(l -> new ProfileDto.LinkDto(l.type, l.url, l.label)).toList(),
                mapTranslated(experiences.findAll(BY_ORDER), e -> e.translations, lang,
                        (e, t) -> new ProfileDto.ExperienceDto(e.company, e.companyUrl, e.location,
                                e.startDate, e.endDate, t.role(), t.descriptionMd())),
                mapTranslated(educations.findAll(BY_ORDER), e -> e.translations, lang,
                        (e, t) -> new ProfileDto.EducationDto(e.institution, e.startDate, e.endDate,
                                t.degree(), t.descriptionMd())),
                mapTranslated(categories.findAll(BY_ORDER), c -> c.translations, lang,
                        (c, t) -> new ProfileDto.SkillCategoryDto(c.code, t.name(),
                                c.skills.stream().map(s -> new ProfileDto.SkillDto(s.name, s.level)).toList())));
    }

    /** Traduzione richiesta, altrimenti quella inglese (fallback). */
    private static <T> T pick(Map<String, T> translations, String lang) {
        T t = translations.get(lang);
        return t != null ? t : translations.get(FALLBACK_LANG);
    }

    /** Mappa le entità scartando quelle senza alcuna traduzione utilizzabile. */
    private static <E, T, R> List<R> mapTranslated(List<E> entities, Function<E, Map<String, T>> translations,
                                                   String lang, java.util.function.BiFunction<E, T, R> mapper) {
        return entities.stream().map(e -> {
            T t = pick(translations.apply(e), lang);
            return t == null ? null : mapper.apply(e, t);
        }).filter(Objects::nonNull).toList();
    }
}
