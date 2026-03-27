package com.javacore.spring_api_app.service;

import com.javacore.spring_api_app.dto.request.RegisterUserRequest;
import com.javacore.spring_api_app.dto.response.RegisterUserResponse;

public interface AuthService {
    RegisterUserResponse register(RegisterUserRequest request);
}
