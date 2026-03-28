package com.javacore.spring_api_app.exception.custom;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException() {
        super(
                "Não foi possivel realizar a operação",
                "EMAIL_ALREADY_EXISTS",
                HttpStatus.CONFLICT
        );
    }
}
