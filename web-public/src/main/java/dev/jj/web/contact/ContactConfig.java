package dev.jj.web.contact;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class ContactConfig {

    @Bean
    FormRateLimiter formRateLimiter(ContactFormProperties p) {
        return new FormRateLimiter(p.perIp(), p.global(), p.window(), p.maxTrackedIps(), Clock.systemUTC());
    }
}
