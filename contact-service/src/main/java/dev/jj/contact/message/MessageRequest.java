package dev.jj.contact.message;

import jakarta.validation.constraints.*;

/** Corpo della POST. captchaToken è il token generato dal widget Turnstile nel browser. */
public record MessageRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 150) String subject,
        @NotBlank @Size(max = 5000) String body,
        @NotNull @Pattern(regexp = "it|en") String lang,
        @NotBlank @Size(max = 2048) String captchaToken) {
}
