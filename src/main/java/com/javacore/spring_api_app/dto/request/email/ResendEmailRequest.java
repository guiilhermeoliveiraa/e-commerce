package com.javacore.spring_api_app.dto.request.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResendEmailRequest(
        @NotBlank
        @NotNull
        @Email
        String email
) {
}
