package dev.jj.web.site;

import dev.jj.web.WebProperties;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Attributi disponibili in ogni template. */
@ControllerAdvice
class SiteModelAdvice {

    private final boolean staticMode;

    SiteModelAdvice(WebProperties props) {
        this.staticMode = props.staticMode();
    }

    /** true nella copia statica (GitHub Pages): i template nascondono il form e usano l'informativa senza form. */
    @ModelAttribute("staticMode")
    boolean staticMode() {
        return staticMode;
    }
}
