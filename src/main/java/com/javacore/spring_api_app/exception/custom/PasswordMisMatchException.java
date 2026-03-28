package com.javacore.spring_api_app.exception.custom;

import org.springframework.http.HttpStatus;

public class PasswordMisMatchException extends BusinessException{
    public PasswordMisMatchException() {
        super(
                "Dados informados são inválidos",
                "PASSWORD_MISMATCH",
                HttpStatus.BAD_REQUEST
        );
    }
}
