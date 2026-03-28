package com.javacore.spring_api_app.exception.custom;

import org.springframework.http.HttpStatus;

public class EmailAlreadyVerifiedException extends BusinessException{
    public EmailAlreadyVerifiedException() {
        super(
                "Operação não permitida",
                "EMAIL_ALREADY_VERIFIED",
                HttpStatus.BAD_REQUEST
        );
    }
}
