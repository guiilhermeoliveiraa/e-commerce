package com.javacore.spring_api_app.service.auth;

import com.javacore.spring_api_app.dto.request.email.ResendEmailRequest;
import com.javacore.spring_api_app.dto.request.email.VerifyEmailRequest;
import com.javacore.spring_api_app.dto.request.token.RefreshTokenRequest;
import com.javacore.spring_api_app.dto.request.user.LoginUserRequest;
import com.javacore.spring_api_app.dto.request.user.RegisterUserRequest;
import com.javacore.spring_api_app.dto.response.token.LogoutRequest;
import com.javacore.spring_api_app.dto.response.user.RegisterUserResponse;
import com.javacore.spring_api_app.dto.response.token.TokenResponse;
import com.javacore.spring_api_app.dto.response.user.LoginUserResponse;

public interface AuthService {
    RegisterUserResponse register(RegisterUserRequest request);

    LoginUserResponse login(LoginUserRequest request);

    TokenResponse refresh(RefreshTokenRequest request);

    void logout(LogoutRequest request);

    void verifyEmail(VerifyEmailRequest request);

    void resendEmail(ResendEmailRequest request);
}
