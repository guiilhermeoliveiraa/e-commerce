package com.javacore.spring_api_app.service.auth;

import com.javacore.spring_api_app.dto.request.user.LoginUserRequest;
import com.javacore.spring_api_app.dto.request.user.RegisterUserRequest;
import com.javacore.spring_api_app.dto.response.LoginUserResponse;
import com.javacore.spring_api_app.dto.response.RegisterUserResponse;

public interface AuthService {
    RegisterUserResponse register(RegisterUserRequest request);

    LoginUserResponse login(LoginUserRequest request);
}
