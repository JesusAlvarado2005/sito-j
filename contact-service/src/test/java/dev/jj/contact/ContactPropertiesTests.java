package dev.jj.contact;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/** Il servizio deve rifiutarsi di partire con una chiave HMAC mancante, corta o d'esempio. */
class ContactPropertiesTests {

    @Configuration
    @EnableConfigurationProperties(ContactProperties.class)
    static class Config {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(Config.class)
            .withPropertyValues("contact.rate-limit.per-ip=5", "contact.rate-limit.global=50",
                    "contact.rate-limit.window=PT1H", "contact.retention=P365D",
                    "contact.notification.max-attempts=12");

    @Test
    void startsWithStrongSalt() {
        runner.withPropertyValues("contact.ip-salt=9f3c1a7e5b2d4c6a8e0f1b3d5a7c9e1f")
                .run(ctx -> assertThat(ctx).hasNotFailed().hasSingleBean(ContactProperties.class));
    }

    @Test
    void refusesMissingShortOrExampleSalt() {
        runner.run(ctx -> assertThat(ctx).hasFailed());
        runner.withPropertyValues("contact.ip-salt=short").run(ctx -> assertThat(ctx).hasFailed());
        runner.withPropertyValues("contact.ip-salt=change-me-salt-but-long-enough")
                .run(ctx -> assertThat(ctx).hasFailed());
    }
}
