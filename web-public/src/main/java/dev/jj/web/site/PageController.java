package dev.jj.web.site;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import dev.jj.web.contact.ContactForm;
import dev.jj.web.contact.ContactFormProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Locale;

@Controller
class PageController {

    private final SiteService site;
    private final ContactFormProperties contact;

    PageController(SiteService site, ContactFormProperties contact) {
        this.site = site;
        this.contact = contact;
    }

    /** "/" manda alla lingua del browser: italiano se lo preferisce, altrimenti inglese. */
    @GetMapping("/")
    RedirectView root(@RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
        String lang = "en";
        if (acceptLanguage != null) {
            try {
                lang = Locale.LanguageRange.parse(acceptLanguage).stream()
                        .map(r -> r.getRange().split("-")[0])
                        .filter(l -> l.equals("it") || l.equals("en")).findFirst().orElse("en");
            } catch (IllegalArgumentException ignored) {
                // header malformato: si resta sull'inglese
            }
        }
        RedirectView redirect = new RedirectView("/" + lang);
        redirect.setContextRelative(true);   // rispetta un eventuale context-path (es. /sito-j)
        redirect.setStatusCode(HttpStatus.FOUND);
        return redirect;
    }

    @GetMapping("/{lang:it|en}")
    String home(@PathVariable String lang, @RequestParam(required = false) String sent, Model model) {
        model.addAttribute("v", site.home(lang));
        model.addAttribute("form", new ContactForm());
        model.addAttribute("siteKey", contact.turnstileSiteKey());
        model.addAttribute("sent", sent != null);
        return "home";
    }

    @GetMapping("/{lang:it|en}/privacy")
    String privacy(@PathVariable String lang, Model model) {
        model.addAttribute("v", site.home(lang));
        return "privacy";
    }
}
