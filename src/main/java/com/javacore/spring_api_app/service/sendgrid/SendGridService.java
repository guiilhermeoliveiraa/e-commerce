package com.javacore.spring_api_app.service.sendgrid;

import com.javacore.spring_api_app.dto.request.sendgrid.SendGridEmailRequest;

public interface SendGridService {
    void sendEmail(SendGridEmailRequest request);
}
