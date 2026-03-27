package com.javacore.spring_api_app.config;

import com.javacore.spring_api_app.properties.sendgrid.SendGridProperties;
import com.sendgrid.SendGrid;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SendGridProperties.class)
public class SendGridConfiguration {

    private final SendGridProperties properties;

    public SendGridConfiguration(SendGridProperties properties) {
        this.properties = properties;
    }

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(properties.apiKey());
    }
}
