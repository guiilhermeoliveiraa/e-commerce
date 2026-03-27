package com.javacore.spring_api_app.service.token;

import com.javacore.spring_api_app.entity.user.User;

public interface TokenService {
    String generateToken(User user);
}
