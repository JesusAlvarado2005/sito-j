package dev.jj.contact.message;

import dev.jj.contact.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class NotificationServiceTests {

    @Autowired NotificationService notifications;
    @Autowired RetentionService retention;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean MailNotifier notifier;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM contact.contact_message");
        jdbc.update("""
                INSERT INTO contact.contact_message (id, name, email, subject, body, lang, ip_hash)
                VALUES ('00000000-0000-0000-0000-000000000001', 'Mario', 'm@example.com', 'Hi', 'body', 'it', 'h')""");
    }

    private <T> T col(String column, Class<T> type) {
        return jdbc.queryForObject("SELECT " + column + " FROM contact.contact_message", type);
    }

    @Test
    void marksMessageAsNotifiedAndNeverSendsTwice() {
        assertThat(notifications.sendPending()).isEqualTo(1);
        assertThat(col("notified_at IS NOT NULL", Boolean.class)).isTrue();

        assertThat(notifications.sendPending()).isZero();
        verify(notifier, times(1)).send(any());
    }

    @Test
    void failureSchedulesRetryWithBackoffInsteadOfRetryingImmediately() {
        doThrow(new IllegalStateException("SMTP down")).when(notifier).send(any());

        assertThat(notifications.sendPending()).isZero();
        assertThat(col("notify_attempts", Integer.class)).isEqualTo(1);
        assertThat(col("next_attempt_at > now()", Boolean.class)).isTrue();

        notifications.sendPending();                       // backoff non scaduto: nessun nuovo tentativo
        verify(notifier, times(1)).send(any());
    }

    @Test
    void stopsAfterMaxAttempts() {
        jdbc.update("UPDATE contact.contact_message SET notify_attempts = 12, next_attempt_at = NULL");

        notifications.sendPending();
        verify(notifier, never()).send(any());
    }

    @Test
    void backoffGrowsExponentiallyAndIsCapped() {
        assertThat(NotificationService.backoff(1)).isEqualTo(Duration.ofMinutes(1));
        assertThat(NotificationService.backoff(4)).isEqualTo(Duration.ofMinutes(8));
        assertThat(NotificationService.backoff(30)).isEqualTo(Duration.ofHours(6));
    }

    @Test
    void retentionDeletesOnlyOldMessages() {
        jdbc.update("""
                INSERT INTO contact.contact_message (id, name, email, subject, body, lang, ip_hash, created_at)
                VALUES ('00000000-0000-0000-0000-000000000002', 'Old', 'o@example.com', 'Hi', 'b', 'en', 'h',
                        now() - interval '400 days')""");

        assertThat(retention.purgeExpired()).isEqualTo(1);
        assertThat(col("name", String.class)).isEqualTo("Mario");
    }

    @Test
    void mailHeadersCannotContainLineBreaks() {
        assertThat(MailNotifier.oneLine("Hi\r\nBcc: evil@example.com")).doesNotContain("\n", "\r");
    }
}
