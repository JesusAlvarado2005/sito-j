package dev.jj.contact.message;

import dev.jj.contact.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class NotificationServiceTests {

    @Autowired NotificationService service;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean MailNotifier notifier;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM contact.contact_message");
        jdbc.update("""
                INSERT INTO contact.contact_message (id, name, email, subject, body, lang, ip_hash)
                VALUES ('00000000-0000-0000-0000-000000000001', 'Mario', 'm@example.com', 'Hi', 'body', 'it', 'h')""");
    }

    private boolean notified() {
        return jdbc.queryForObject("SELECT notified_at IS NOT NULL FROM contact.contact_message", Boolean.class);
    }

    @Test
    void marksMessageAsNotifiedAfterSending() {
        assertThat(service.sendPending()).isEqualTo(1);
        assertThat(notified()).isTrue();

        assertThat(service.sendPending()).isZero();           // già notificato: non si reinvia
        verify(notifier, org.mockito.Mockito.times(1)).send(any());
    }

    @Test
    void keepsMessagePendingWhenSendingFailsSoItIsRetried() {
        doThrow(new IllegalStateException("SMTP down")).when(notifier).send(any());

        assertThat(service.sendPending()).isZero();
        assertThat(notified()).isFalse();
    }

    @Test
    void mailHeadersCannotContainLineBreaks() {
        assertThat(MailNotifier.oneLine("Hi\r\nBcc: evil@example.com")).doesNotContain("\n", "\r");
        verify(notifier, never()).send(any());
    }
}
