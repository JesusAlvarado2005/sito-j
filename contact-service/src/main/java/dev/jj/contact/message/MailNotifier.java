package dev.jj.contact.message;

import dev.jj.contact.ContactProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
class MailNotifier implements Notifier {

    private final JavaMailSender mailSender;
    private final String to;
    private final String from;

    MailNotifier(JavaMailSender mailSender, ContactProperties props) {
        this.mailSender = mailSender;
        this.to = props.notification().to() == null ? "" : props.notification().to();
        this.from = props.notification().from() == null ? "" : props.notification().from();
    }

    @Override
    public void send(ContactMessage m) {
        if (to.isBlank()) {
            throw new IllegalStateException("contact.notification.to non configurato");
        }
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setFrom(from);
        mail.setReplyTo(oneLine(m.email));            // rispondi direttamente al visitatore
        mail.setSubject("[sito-j] " + oneLine(m.subject));
        mail.setText("Da: " + oneLine(m.name) + " <" + oneLine(m.email) + ">\nLingua: " + m.lang
                + "\nRicevuto: " + m.createdAt + "\n\n" + m.body);
        mailSender.send(mail);
    }

    /** Toglie gli a-capo dai campi usati negli header dell'email (previene l'header injection). */
    static String oneLine(String s) {
        return s.replaceAll("[\\r\\n]+", " ");
    }
}
