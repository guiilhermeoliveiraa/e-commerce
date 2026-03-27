package com.javacore.spring_api_app.properties.sendgrid;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.sendgrid")
public record SendGridProperties(
        String apiKey,
        String fromEmail
) {
}
