package com.javacore.spring_api_app.service.email;

import com.javacore.spring_api_app.entity.email.EmailValidation;
import com.javacore.spring_api_app.entity.user.User;

public interface EmailValidationService {
    EmailValidation createValidation(User user);

    void validateCode(Long userId, String code);
}
