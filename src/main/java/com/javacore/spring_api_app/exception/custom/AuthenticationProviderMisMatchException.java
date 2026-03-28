package com.javacore.spring_api_app.exception.custom;

import org.springframework.http.HttpStatus;

public class AuthenticationProviderMisMatchException extends BusinessException{
    public AuthenticationProviderMisMatchException() {
        super(
                "Não foi possivel autenticar o usuário",
                "AUTHENTICATION_PROVIDER_MISMATCH",
                HttpStatus.UNAUTHORIZED
        );
    }
}
