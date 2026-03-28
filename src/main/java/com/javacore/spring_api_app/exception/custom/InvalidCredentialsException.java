package com.javacore.spring_api_app.exception.custom;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super(
                "Credenciais inválidas",
                "INVALID_CREDENTIALS",
                HttpStatus.UNAUTHORIZED
        );
    }
}
