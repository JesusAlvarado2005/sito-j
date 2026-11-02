package dev.jj.profile.profile;

import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
class ProfileController {

    private final ProfileService service;

    ProfileController(ProfileService service) {
        this.service = service;
    }

    /** Lingua non valida -> 400 ProblemDetail (validazione dei parametri). */
    @GetMapping
    ProfileDto get(@RequestParam(defaultValue = "en") @Pattern(regexp = "it|en") String lang) {
        return service.getProfile(lang);
    }
}
