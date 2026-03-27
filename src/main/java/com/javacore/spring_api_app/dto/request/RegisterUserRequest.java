package com.javacore.spring_api_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(

        @NotNull(message = "Email não pode ser nulo")
        @NotBlank(message = "Email é obrigatório")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email inválido")
        String email,

        @NotNull(message = "Primeiro nome não pode ser nulo")
        @NotBlank(message = "Primeiro nome é obrigatório")
        @Size(min = 2, message = "Primeiro nome deve conter ao menos 2 caracteres")
        @Pattern(regexp = "^[\\p{L} ]+$", message = "Primeiro nome inválido")
        String firstName,

        @NotNull(message = "Ultimo nome não pode ser nulo")
        @NotBlank(message = "Ultimo nome é obrigatório")
        @Size(min = 2, message = "Ultimo nome deve conter ao menos 2 caracteres")
        @Pattern(regexp = "^[\\p{L} ]+$", message = "Ultimo nome inválido")
        String lastName,

        @NotNull(message = "Senha não pode ser nula")
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve conter ao menos 8 caracteres")
        @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}$",
                message = "Senha inválida")
        String password,

        @NotNull(message = "Confirmar senha não pode ser nulo")
        @NotBlank(message = "Confirmar senha é obrigatória")
        String confirmPassword
) {
}
