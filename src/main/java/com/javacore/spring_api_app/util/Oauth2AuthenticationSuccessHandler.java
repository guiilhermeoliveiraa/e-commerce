package com.javacore.spring_api_app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javacore.spring_api_app.dto.response.user.LoginUserResponse;
import com.javacore.spring_api_app.service.oauth2.Oauth2Service;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Oauth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final Oauth2Service oauth2Service;
    private final ObjectMapper objectMapper;

    public Oauth2AuthenticationSuccessHandler(Oauth2Service oauth2Service, ObjectMapper objectMapper) {
        this.oauth2Service = oauth2Service;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        if (authentication instanceof OAuth2AuthenticationToken auth2Token) {
            OAuth2User auth2User = auth2Token.getPrincipal();

            String email = auth2User.getAttribute("email");
            String name = auth2User.getAttribute("name");

            if (email == null || name == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Credenciais inválidas");
                return;
            }

            LoginUserResponse loginResponse = oauth2Service.loginWithGoogle(email, name);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            var write = response.getWriter();
            write.write(objectMapper.writeValueAsString(loginResponse));
            write.flush();
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Operação inválida");
        }
    }
}