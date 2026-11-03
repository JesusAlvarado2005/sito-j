package dev.jj.web.site;

import dev.jj.web.WebProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class SeoController {

    private final String base;

    SeoController(WebProperties props) {
        this.base = props.baseUrl();
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    String robots() {
        return "User-agent: *\nAllow: /\n\nSitemap: " + base + "/sitemap.xml\n";
    }

    /** Ogni lingua dichiara l'alternativa (hreflang): Google serve la versione giusta a chi cerca. */
    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    String sitemap() {
        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8"?>
                <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:xhtml="http://www.w3.org/1999/xhtml">
                """);
        for (String lang : new String[]{"it", "en"}) {
            xml.append("  <url>\n    <loc>").append(base).append('/').append(lang).append("</loc>\n");
            for (String alt : new String[]{"it", "en"}) {
                xml.append("    <xhtml:link rel=\"alternate\" hreflang=\"").append(alt).append("\" href=\"")
                        .append(base).append('/').append(alt).append("\"/>\n");
            }
            xml.append("  </url>\n");
        }
        return xml.append("</urlset>\n").toString();
    }
}
