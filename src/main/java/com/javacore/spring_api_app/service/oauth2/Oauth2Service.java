package com.javacore.spring_api_app.service.oauth2;

import com.javacore.spring_api_app.dto.response.user.LoginUserResponse;

public interface Oauth2Service {
    LoginUserResponse loginWithGoogle(String email, String name);
}
