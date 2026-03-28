package com.javacore.spring_api_app.dto.request.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequest(

        @NotBlank
        @NotNull
        @Email
        String email,

        @NotNull
        @NotBlank
        @Size(min = 5, max = 5)
        String code
) {
}
