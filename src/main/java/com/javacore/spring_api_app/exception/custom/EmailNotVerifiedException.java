package com.javacore.spring_api_app.exception.custom;

import org.springframework.http.HttpStatus;

public class EmailNotVerifiedException extends BusinessException{
    public EmailNotVerifiedException() {
        super(
                "Não foi possivel autenticar usuário",
                "EMAIL_NOT_VERIFIED",
                HttpStatus.UNAUTHORIZED
        );
    }
}
