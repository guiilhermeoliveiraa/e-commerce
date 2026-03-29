package com.javacore.spring_api_app.service.auth;

import com.javacore.spring_api_app.domain.name.NameNormalizer;
import com.javacore.spring_api_app.dto.request.email.ResendEmailRequest;
import com.javacore.spring_api_app.dto.request.email.VerifyEmailRequest;
import com.javacore.spring_api_app.dto.request.sendgrid.SendGridEmailRequest;
import com.javacore.spring_api_app.dto.request.token.RefreshTokenRequest;
import com.javacore.spring_api_app.dto.request.user.LoginUserRequest;
import com.javacore.spring_api_app.dto.request.user.RegisterUserRequest;
import com.javacore.spring_api_app.dto.response.token.LogoutRequest;
import com.javacore.spring_api_app.dto.response.user.RegisterUserResponse;
import com.javacore.spring_api_app.dto.response.token.TokenResponse;
import com.javacore.spring_api_app.dto.response.user.LoginUserResponse;
import com.javacore.spring_api_app.entity.token.RefreshToken;
import com.javacore.spring_api_app.entity.user.User;
import com.javacore.spring_api_app.entity.user.UserProvider;
import com.javacore.spring_api_app.exception.custom.*;
import com.javacore.spring_api_app.repository.token.RefreshTokenRepository;
import com.javacore.spring_api_app.repository.user.UserRepository;
import com.javacore.spring_api_app.service.email.EmailValidationService;
import com.javacore.spring_api_app.service.refresh.RefreshTokenService;
import com.javacore.spring_api_app.service.sendgrid.SendGridService;
import com.javacore.spring_api_app.service.token.TokenService;
import com.javacore.spring_api_app.util.EmailContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final SendGridService sendGridService;
    private final EmailValidationService emailValidationService;
    private final JwtDecoder jwtDecoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            SendGridService sendGridService,
            EmailValidationService emailValidationService,
            JwtDecoder jwtDecoder,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.sendGridService = sendGridService;
        this.emailValidationService = emailValidationService;
        this.jwtDecoder = jwtDecoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public RegisterUserResponse register(RegisterUserRequest request) {
        String normalizedFirstName = NameNormalizer.normalize(request.firstName());
        String normalizedLastName = NameNormalizer.normalize(request.lastName());
        EmailContext emailCtx = EmailContext.of(request.email());

        log.info("event=register_attempt email={}", emailCtx.masked());

        if (userRepository.existsByEmailAndDeletedFalse(emailCtx.normalized())) {
            log.warn("event=register_failed reason=email_already_exists email={}", emailCtx.masked());
            throw new EmailAlreadyExistsException();
        }

        if (!request.password().equals(request.confirmPassword())) {
            log.warn("event=register_failed reason=password_mismatch email={}", emailCtx.masked());
            throw new PasswordMisMatchException();
        }

        User user = User.builder()
                .email(emailCtx.normalized())
                .firstName(normalizedFirstName)
                .lastName(normalizedLastName)
                .password(passwordEncoder.encode(request.password()))
                .emailVerified(false)
                .userProvider(UserProvider.LOCAL)
                .build();

        user = userRepository.save(user);

        log.info("event=register_success userPublicId={} email={}",
                user.getPublicId(), emailCtx.masked());

        var validation = emailValidationService.createValidation(user);

        sendGridService.sendEmail(new SendGridEmailRequest(
                user.getEmail(),
                user.getFirstName(),
                validation.getVerificationCode()
        ));

        log.debug("event=verification_email_sent userPublicId={}", user.getPublicId());

        return toResponse(user);
    }

    @Override
    public LoginUserResponse login(LoginUserRequest request) {
        EmailContext emailCtx = EmailContext.of(request.email());

        log.info("event=login_attempt email={}", emailCtx.masked());

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    emailCtx.normalized(),
                    request.password()
            ));
        } catch (AuthenticationException exception) {
            log.warn("event=login_failed reason=invalid_credentials email={}", emailCtx.masked());
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmailAndDeletedFalse(emailCtx.normalized())
                .orElseThrow(() -> {
                    log.warn("event=login_failed reason=user_not_found email={}", emailCtx.masked());
                    return new InvalidCredentialsException();
                });

        if (user.getUserProvider() == UserProvider.GOOGLE) {
            log.warn("event=login_failed reason=provider_mismatch email={} provider={}",
                    emailCtx.masked(), user.getUserProvider());
            throw new AuthenticationProviderMisMatchException();
        }

        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            log.warn("event=login_failed reason=email_not_verified userPublicId={}",
                    user.getPublicId());
            throw new EmailNotVerifiedException();
        }

        String accessToken = tokenService.generateToken(user);
        String refreshToken = refreshTokenService.create(user);

        log.info("event=login_success userPublicId={}", user.getPublicId());

        return new LoginUserResponse(accessToken, refreshToken);
    }

    @Override
    public TokenResponse refresh(RefreshTokenRequest request) {
        log.debug("event=refresh_attempt");

        Jwt jwt = jwtDecoder.decode(request.refreshToken());

        if (!"refresh".equals(jwt.getClaim("type"))) {
            log.warn("event=refresh_failed reason=invalid_type");
            throw new InvalidRefreshTokenException();
        }

        String jti = jwt.getClaim("jti");

        RefreshToken storedToken = refreshTokenRepository.findByTokenId(jti)
                .orElseThrow(() -> {
                    log.warn("event=refresh_failed reason=token_not_found jti={}", jti);
                    return new InvalidCredentialsException();
                });

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            log.warn("event=refresh_failed reason=token_expired userPublicId={}",
                    storedToken.getUser().getPublicId());
            throw new InvalidRefreshTokenException();
        }

        if (Boolean.TRUE.equals(storedToken.getRevoked())) {

            User user = storedToken.getUser();

            log.error("event=refresh_failed reason=suspicious_token_reuse userPublicId={}",
                    user.getPublicId());

            List<RefreshToken> tokens = refreshTokenRepository.findAllByUser(user);

            tokens.forEach(token -> token.setRevoked(true));
            refreshTokenRepository.saveAll(tokens);

            throw new SuspiciousTokenReuseException();
        }

        User user = storedToken.getUser();

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        String newAccessToken = tokenService.generateToken(user);
        String newRefreshToken = refreshTokenService.create(user);

        log.info("event=refresh_success userPublicId={}", user.getPublicId());

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(LogoutRequest request) {
        log.info("event=logout_attempt");

        Jwt jwt = jwtDecoder.decode(request.refreshToken());

        if (!"refresh".equals(jwt.getClaim("type"))) {
            log.warn("event=logout_failed reason=invalid_token");
            throw new InvalidRefreshTokenException();
        }

        String jti = jwt.getClaim("jti");

        refreshTokenRepository.findByTokenId(jti)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    log.info("event=logout_success userPublicId={}",
                            token.getUser().getPublicId());
                });
    }

    @Override
    public void verifyEmail(VerifyEmailRequest request) {
        EmailContext emailCtx = EmailContext.of(request.email());

        log.info("event=email_verification_attempt email={}", emailCtx.masked());

        User user = findUserByEmailAndThrow(request.email());

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            log.warn("event=email_verification_failed reason=already_verified userPublicId={}",
                    user.getPublicId());
            throw new EmailAlreadyVerifiedException();
        }

        emailValidationService.validateCode(user.getId(), request.code());
        log.info("event=email_verification_success userPublicId={}",
                user.getPublicId());
    }

    @Override
    public void resendEmail(ResendEmailRequest request) {
        EmailContext emailCtx = EmailContext.of(request.email());

        log.info("event=resend_email_attempt email={}", emailCtx.masked());

        User user = findUserByEmailAndThrow(request.email());

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            log.warn("event=resend_email_failed reason=already_verified userPublicId={}",
                    user.getPublicId());
            throw new EmailAlreadyExistsException();
        }

        var validation = emailValidationService.createValidation(user);

        sendGridService.sendEmail(new SendGridEmailRequest(
                user.getEmail(),
                user.getFirstName(),
                validation.getVerificationCode()
        ));
        log.info("event=resend_email_success userPublicId={}",
                user.getPublicId());
    }

    private RegisterUserResponse toResponse(User user) {
        return new RegisterUserResponse(
                user.getPublicId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getEmailVerified(),
                user.getUserProvider(),
                user.getDeleted()
        );
    }

    private User findUserByEmailAndThrow(String email) {
        EmailContext emailCtx = EmailContext.of(email);

        return userRepository.findByEmailAndDeletedFalse(emailCtx.normalized())
                .orElseThrow(() -> {
                    log.warn("event=user_lookup_failed reason=user_not_found email={}", emailCtx.masked());
                    return new InvalidCredentialsException();
                });
    }
}