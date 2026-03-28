package com.javacore.spring_api_app.service.refresh;

import com.javacore.spring_api_app.entity.token.RefreshToken;
import com.javacore.spring_api_app.entity.user.User;
import com.javacore.spring_api_app.repository.token.RefreshTokenRepository;
import com.javacore.spring_api_app.service.token.TokenService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final JwtDecoder jwtDecoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;

    public RefreshTokenServiceImpl(
            JwtDecoder jwtDecoder,
            RefreshTokenRepository refreshTokenRepository,
            TokenService tokenService) {
        this.jwtDecoder = jwtDecoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenService = tokenService;
    }

    @Override
    public String create(User user) {
        String refreshToken = tokenService.generateRefreshToken(user);

        Jwt jwt = jwtDecoder.decode(refreshToken);
        String jti = jwt.getClaim("jti");

        RefreshToken newToken = RefreshToken.builder()
                .tokenId(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(jwt.getExpiresAt())
                .revoked(false)
                .build();
        refreshTokenRepository.save(newToken);

        return refreshToken;
    }
}
