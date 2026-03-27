package com.javacore.spring_api_app.domain.email;

public final class EmailNormalizer {

    private EmailNormalizer() {}

    public static String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
