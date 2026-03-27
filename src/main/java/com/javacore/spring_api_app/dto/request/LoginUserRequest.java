package com.javacore.spring_api_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginUserRequest(

        @NotNull(message = "Email não pode ser nulo")
        @NotBlank(message = "Email é obrigatório")
        String email,

        @NotNull(message = "Senha não pode ser nula")
        @NotBlank(message = "Senha é obrigatória")
        String password
) {
}
