package com.javacore.spring_api_app.service.oauth2;

import com.javacore.spring_api_app.dto.response.LoginUserResponse;

public interface Oauth2Service {
    LoginUserResponse loginWithGoogle(String email, String name);
}
