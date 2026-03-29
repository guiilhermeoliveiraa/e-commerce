package com.javacore.spring_api_app.util;

import com.javacore.spring_api_app.domain.email.EmailNormalizer;
import com.javacore.spring_api_app.domain.email.MaskEmail;

public record EmailContext(String normalized, String masked) {
    public static EmailContext of(String email) {
        String normalized = EmailNormalizer.normalize(email);
        String masked = MaskEmail.mask(email);
        return new EmailContext(normalized, masked);
    }
}