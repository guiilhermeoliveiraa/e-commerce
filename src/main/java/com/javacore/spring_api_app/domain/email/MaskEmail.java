package com.javacore.spring_api_app.domain.email;

public final class MaskEmail {

    private MaskEmail() {}

    public static String mask(String email) {
        if (email == null || email.isBlank()) {
            return "null";
        }

        int atIndex = email.indexOf("@");

        if (atIndex <= 1) {
            return "***";
        }

        String namePart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);

        String visiblePart = namePart.length() <= 2 ? namePart.charAt(0) + "*" : namePart.substring(0, 2);

        return visiblePart + "***" + domainPart;
    }
}