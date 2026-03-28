package com.javacore.spring_api_app.exception.custom;

import org.springframework.http.HttpStatus;

public class InvalidVerificationCodeException extends BusinessException{
    public InvalidVerificationCodeException() {
        super(
                "Código inválido ou expirado",
                "INVALID_VERIFICATION_CODE",
                HttpStatus.BAD_REQUEST
        );
    }
}
