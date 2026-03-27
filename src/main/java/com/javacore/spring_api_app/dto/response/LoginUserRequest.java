package com.javacore.spring_api_app.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginUserRequest(
        @NotNull(message = "Token não pode ser nulo")
        @NotBlank(message = "Token é obrigatório")
        String token
) {
}
