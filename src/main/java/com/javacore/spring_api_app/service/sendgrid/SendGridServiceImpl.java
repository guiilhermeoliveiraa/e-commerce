package com.javacore.spring_api_app.service.sendgrid;

import com.javacore.spring_api_app.domain.email.MaskEmail;
import com.javacore.spring_api_app.dto.request.sendgrid.SendGridEmailRequest;
import com.javacore.spring_api_app.exception.custom.BusinessException;
import com.javacore.spring_api_app.properties.sendgrid.SendGridProperties;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class SendGridServiceImpl implements SendGridService {

    private final SendGrid sendGrid;
    private final SendGridProperties properties;

    public SendGridServiceImpl(SendGrid sendGrid, SendGridProperties properties) {
        this.sendGrid = sendGrid;
        this.properties = properties;
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendEmail(SendGridEmailRequest request) {
        String maskedEmail = MaskEmail.mask(request.to());

        Mail mail = buildEmail(request);

        log.debug("event=email_send_attempt provider=sendgrid to={}", maskedEmail);

        Request sdRequest = new Request();

        try {
            sdRequest.setMethod(Method.POST);
            sdRequest.setEndpoint("mail/send");
            sdRequest.setBody(mail.build());

            Response response = sendGrid.api(sdRequest);

            int statusCode = response.getStatusCode();

            if (statusCode != 202) {
                log.error(
                        "event=email_send_failed provider=sendgrid reason=unexpected_status statusCode={} to={}",
                        statusCode,
                        maskedEmail
                );
            } else {
                log.info(
                        "event=email_send_success provider=sendgrid to={}",
                        maskedEmail
                );
            }
        } catch (IOException e) {
            log.error(
                    "event=email_send_failed provider=sendgrid reason=io_exception to={}",
                    maskedEmail,
                    e
            );
        }
    }

    private Mail buildEmail(SendGridEmailRequest request) {
        Email from = new Email(properties.fromEmail());
        Email toEmail = new Email(request.to());

        Mail mail = new Mail();
        mail.setFrom(from);

        mail.setTemplateId(properties.templateId());

        var personalization = new com.sendgrid.helpers.mail.objects.Personalization();
        personalization.addTo(toEmail);

        personalization.addDynamicTemplateData("name", request.name());
        personalization.addDynamicTemplateData("code", request.code());

        mail.addPersonalization(personalization);
        return mail;
    }
}