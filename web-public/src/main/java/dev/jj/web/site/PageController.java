package dev.jj.web.site;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Locale;

@Controller
class PageController {

    private final SiteService site;

    PageController(SiteService site) {
        this.site = site;
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
        redirect.setStatusCode(HttpStatus.FOUND);
        return redirect;
    }

    @GetMapping("/{lang:it|en}")
    String home(@PathVariable String lang, Model model) {
        model.addAttribute("v", site.home(lang));
        return "home";
    }
}
