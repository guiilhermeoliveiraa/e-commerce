package com.javacore.spring_api_app.exception.custom;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends BusinessException{
    public InvalidRefreshTokenException() {
        super(
                "Não foi possivel autenticar usuário",
                "INVALID_REFRESH_TOKEN",
                HttpStatus.UNAUTHORIZED
        );
    }
}
