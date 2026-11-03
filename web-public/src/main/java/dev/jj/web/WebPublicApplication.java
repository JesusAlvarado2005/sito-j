package dev.jj.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WebPublicApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebPublicApplication.class, args);
    }
}
