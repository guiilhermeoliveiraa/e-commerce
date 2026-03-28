package com.javacore.spring_api_app.dto.response.token;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
