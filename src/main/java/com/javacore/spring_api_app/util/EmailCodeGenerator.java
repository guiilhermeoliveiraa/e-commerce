package com.javacore.spring_api_app.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class EmailCodeGenerator {

    private static final SecureRandom random = new SecureRandom();

    public String generateCode() {
        int code = 10000 + random.nextInt(90000);
        return String.valueOf(code);
    }
}
