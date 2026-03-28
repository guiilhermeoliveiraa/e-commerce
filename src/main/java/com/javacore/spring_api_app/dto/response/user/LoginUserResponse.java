package com.javacore.spring_api_app.dto.response.user;

public record LoginUserResponse(
        String accessToken,
        String refreshToken
) {
}
