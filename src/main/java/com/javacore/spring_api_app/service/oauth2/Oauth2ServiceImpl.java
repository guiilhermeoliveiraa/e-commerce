package com.javacore.spring_api_app.service.oauth2;

import com.javacore.spring_api_app.domain.name.NameNormalizer;
import com.javacore.spring_api_app.dto.response.user.LoginUserResponse;
import com.javacore.spring_api_app.entity.user.User;
import com.javacore.spring_api_app.entity.user.UserProvider;
import com.javacore.spring_api_app.exception.custom.AuthenticationProviderMisMatchException;
import com.javacore.spring_api_app.repository.user.UserRepository;
import com.javacore.spring_api_app.service.refresh.RefreshTokenService;
import com.javacore.spring_api_app.service.token.TokenService;
import com.javacore.spring_api_app.util.EmailContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
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
        EmailContext emailCtx = EmailContext.of(email);

        log.info("event=oauth2_login_attempt provider=google email={}", emailCtx.masked());

        User user = userRepository.findByEmailAndDeletedFalse(emailCtx.normalized())
                .map(existingUser -> {
                    if (existingUser.getUserProvider() == UserProvider.LOCAL) {
                        log.warn("event=oauth2_login_failed reason=provider_mismatch email={} expected=GOOGLE actual={}",
                                emailCtx.masked(), existingUser.getUserProvider());
                        throw new AuthenticationProviderMisMatchException();
                    }
                    log.info("event=oauth2_login_existing_user userPublicId={}", existingUser.getPublicId());
                    return existingUser;
                })
                .orElseGet(() -> {
                    String normalizedFullName = NameNormalizer.normalize(name);

                    String[] parts = normalizedFullName.split(" ");

                    String firstName = parts[0];
                    String lastName = parts.length > 1 ? parts[parts.length - 1] : "";

                    User newUser = User.builder()
                            .email(emailCtx.masked())
                            .firstName(firstName)
                            .lastName(lastName)
                            .password("")
                            .emailVerified(true)
                            .userProvider(UserProvider.GOOGLE)
                            .build();

                    User savedUser = userRepository.save(newUser);

                    log.info("event=oauth2_register_success provider=google userPublicId={} email={}",
                            savedUser.getPublicId(), emailCtx.masked());
                    return savedUser;
                });

        String accessToken = tokenService.generateToken(user);
        String refreshToken = refreshTokenService.create(user);

        log.info("event=oauth2_login_success provider=google userPublicId={}", user.getPublicId());
        return new LoginUserResponse(accessToken, refreshToken);
    }
}