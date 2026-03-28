package com.javacore.spring_api_app.dto.response.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.javacore.spring_api_app.entity.user.UserProvider;

import java.time.Instant;
import java.util.UUID;

public record RegisterUserResponse(
        UUID publicId,
        String firstName,
        String lastName,
        String email,

        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "America/Sao_Paulo")
        Instant createdAt,

        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "America/Sao_Paulo")
        Instant updatedAt,

        Boolean emailVerified,
        UserProvider userProvider,
        Boolean deleted
) {
}
