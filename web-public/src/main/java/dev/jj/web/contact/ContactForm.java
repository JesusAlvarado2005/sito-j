package dev.jj.web.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dati del form. Classe con getter/setter (non record) perché Thymeleaf la usa con th:object/th:field.
 * I limiti sono gli stessi di contact-service: un errore si mostra subito, senza chiamare il servizio.
 */
public class ContactForm {

    @NotBlank @Size(max = 100)
    private String name;

    @NotBlank @Email @Size(max = 255)
    private String email;

    @NotBlank @Size(max = 150)
    private String subject;

    @NotBlank @Size(max = 5000)
    private String body;

    /** Campo trappola, invisibile alle persone: se è compilato, l'invio arriva da un bot. */
    private String website;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
}
