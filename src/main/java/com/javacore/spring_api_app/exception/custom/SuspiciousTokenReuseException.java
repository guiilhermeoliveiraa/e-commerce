package com.javacore.spring_api_app.exception.custom;

import org.springframework.http.HttpStatus;

public class SuspiciousTokenReuseException extends BusinessException{
    public SuspiciousTokenReuseException() {
        super(
                "Não foi possivel autenticar usuário",
                "SUSPICIOUS_TOKEN_REUSE",
                HttpStatus.UNAUTHORIZED
        );
    }
}
