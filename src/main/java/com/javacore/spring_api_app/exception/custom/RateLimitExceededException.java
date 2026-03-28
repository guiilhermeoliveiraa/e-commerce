package com.javacore.spring_api_app.exception.custom;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends BusinessException{
    public RateLimitExceededException(long awaitSeconds) {
        super(
                "Tente novamente mais tarde",
                "RATE_LIMIT_EXCEEDED",
                HttpStatus.TOO_MANY_REQUESTS
        );
    }
}
