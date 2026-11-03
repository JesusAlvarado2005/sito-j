package dev.jj.web.contact;

import dev.jj.web.site.SiteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Form contatti con Post/Redirect/Get: se l'invio riesce si fa redirect, così un refresh non reinvia il messaggio.
 * Ordine dei controlli, dal più economico: honeypot -> validazione -> token presente -> rate limit -> contact-service.
 */
@Controller
class ContactController {

    private final SiteService site;
    private final ContactClient client;
    private final FormRateLimiter limiter;
    private final ContactFormProperties props;

    ContactController(SiteService site, ContactClient client, FormRateLimiter limiter, ContactFormProperties props) {
        this.site = site;
        this.client = client;
        this.limiter = limiter;
        this.props = props;
    }

    /** Chi apre /it/contact (es. da un segnalibro) finisce sulla sezione contatti della home. */
    @GetMapping("/{lang:it|en}/contact")
    String show(@PathVariable String lang) {
        return "redirect:/" + lang + "#contatti";
    }

    @PostMapping("/{lang:it|en}/contact")
    String submit(@PathVariable String lang,
                  @Valid @ModelAttribute("form") ContactForm form, BindingResult errors,
                  @RequestParam(value = "cf-turnstile-response", required = false) String captchaToken,
                  HttpServletRequest request, HttpServletResponse response, Model model) {
        if (StringUtils.hasText(form.getWebsite())) {
            return sent(lang);   // bot: risposta identica a un successo, così non capisce di essere stato scartato
        }
        if (errors.hasErrors()) {
            return render(lang, 422, "contact.error.fields", response, model);
        }
        if (!StringUtils.hasText(captchaToken) || captchaToken.length() > 2048) {
            return render(lang, 400, "contact.error.captcha", response, model);
        }
        // getRemoteAddr() è già l'IP reale: Tomcat lo ricava da X-Forwarded-For solo se arriva da un proxy interno
        String clientIp = request.getRemoteAddr();
        if (!limiter.tryAcquire(clientIp)) {
            return render(lang, 429, "contact.error.rate", response, model);
        }
        return switch (client.send(form, lang, captchaToken, clientIp)) {
            case SENT -> sent(lang);
            case CAPTCHA_FAILED -> render(lang, 400, "contact.error.captcha", response, model);
            case RATE_LIMITED -> render(lang, 429, "contact.error.rate", response, model);
            case UNAVAILABLE -> render(lang, 503, "contact.error.unavailable", response, model);
        };
    }

    private static String sent(String lang) {
        return "redirect:/" + lang + "?sent#contatti";
    }

    /** Ripresenta la home con il form compilato e il messaggio d'errore. */
    private String render(String lang, int status, String errorKey, HttpServletResponse response, Model model) {
        response.setStatus(status);
        model.addAttribute("v", site.home(lang));
        model.addAttribute("siteKey", props.turnstileSiteKey());
        model.addAttribute("contactError", errorKey);
        return "home";
    }
}
