package com.javacore.spring_api_app.dto.request.sendgrid;

public record SendGridEmailRequest(
        String to,
        String name,
        String code
) {
}
