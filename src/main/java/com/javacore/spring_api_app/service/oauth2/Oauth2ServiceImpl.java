package com.javacore.spring_api_app.service.oauth2;

import com.javacore.spring_api_app.domain.email.EmailNormalizer;
import com.javacore.spring_api_app.domain.name.NameNormalizer;
import com.javacore.spring_api_app.dto.response.user.LoginUserResponse;
import com.javacore.spring_api_app.entity.user.User;
import com.javacore.spring_api_app.entity.user.UserProvider;
import com.javacore.spring_api_app.exception.custom.AuthenticationProviderMisMatchException;
import com.javacore.spring_api_app.repository.user.UserRepository;
import com.javacore.spring_api_app.service.refresh.RefreshTokenService;
import com.javacore.spring_api_app.service.token.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class Oauth2ServiceImpl implements Oauth2Service {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    public Oauth2ServiceImpl(
            UserRepository userRepository,
            TokenService tokenService,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public LoginUserResponse loginWithGoogle(String email, String name) {
        String normalizedEmail = EmailNormalizer.normalize(email);

        User user = userRepository.findByEmailAndDeletedFalse(normalizedEmail)
                .map(existingUser -> {
                    if (existingUser.getUserProvider() == UserProvider.LOCAL) {
                        throw new AuthenticationProviderMisMatchException();
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                    String normalizedFullName = NameNormalizer.normalize(name);

                    String[] parts = normalizedFullName.split(" ");

                    String firstName = parts[0];
                    String lastName = parts.length > 1 ? parts[parts.length - 1] : "";

                    User newUser = User.builder()
                            .email(normalizedEmail)
                            .firstName(firstName)
                            .lastName(lastName)
                            .password("")
                            .emailVerified(true)
                            .userProvider(UserProvider.GOOGLE)
                            .build();

                    return userRepository.save(newUser);
                });

        String accessToken = tokenService.generateToken(user);
        String refreshToken = refreshTokenService.create(user);

        return new LoginUserResponse(accessToken, refreshToken);
    }
}