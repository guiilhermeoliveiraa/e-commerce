package com.javacore.spring_api_app.service.refresh;

import com.javacore.spring_api_app.entity.user.User;

public interface RefreshTokenService {
    String create(User user);
}
