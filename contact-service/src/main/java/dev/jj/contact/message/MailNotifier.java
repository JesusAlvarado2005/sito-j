package dev.jj.contact.message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
class MailNotifier implements Notifier {

    private final JavaMailSender mailSender;
    private final String to;
    private final String from;

    MailNotifier(JavaMailSender mailSender,
                 @Value("${contact.notification.to:}") String to,
                 @Value("${contact.notification.from:}") String from) {
        this.mailSender = mailSender;
        this.to = to;
        this.from = from;
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
