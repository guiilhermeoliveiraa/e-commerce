package com.javacore.spring_api_app.service.auth;

import com.javacore.spring_api_app.domain.email.EmailNormalizer;
import com.javacore.spring_api_app.domain.name.NameNormalizer;
import com.javacore.spring_api_app.dto.request.email.ResendEmailRequest;
import com.javacore.spring_api_app.dto.request.email.VerifyEmailRequest;
import com.javacore.spring_api_app.dto.request.sendgrid.SendGridEmailRequest;
import com.javacore.spring_api_app.dto.request.user.LoginUserRequest;
import com.javacore.spring_api_app.dto.request.user.RegisterUserRequest;
import com.javacore.spring_api_app.dto.response.LoginUserResponse;
import com.javacore.spring_api_app.dto.response.RegisterUserResponse;
import com.javacore.spring_api_app.entity.user.User;
import com.javacore.spring_api_app.exception.custom.*;
import com.javacore.spring_api_app.repository.user.UserRepository;
import com.javacore.spring_api_app.service.email.EmailValidationService;
import com.javacore.spring_api_app.service.sendgrid.SendGridService;
import com.javacore.spring_api_app.service.token.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final SendGridService sendGridService;
    private final EmailValidationService emailValidationService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            SendGridService sendGridService,
            EmailValidationService emailValidationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.sendGridService = sendGridService;
        this.emailValidationService = emailValidationService;
    }

    @Override
    public RegisterUserResponse register(RegisterUserRequest request) {
        String normalizedFirstName = NameNormalizer.normalize(request.firstName());
        String normalizedLastName = NameNormalizer.normalize(request.lastName());
        String normalizedEmail = EmailNormalizer.normalize(request.email());

        if (userRepository.existsByEmailAndDeletedFalse(normalizedEmail)) {
            throw new EmailAlreadyExistsException();
        }

        if (!request.password().equals(request.confirmPassword())) {
            throw new PasswordMisMatchException();
        }

        User user = User.builder()
                .email(normalizedEmail)
                .firstName(normalizedFirstName)
                .lastName(normalizedLastName)
                .password(passwordEncoder.encode(request.password()))
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        var validation = emailValidationService.createValidation(user);

        sendGridService.sendEmail(new SendGridEmailRequest(
                user.getEmail(),
                user.getFirstName(),
                validation.getVerificationCode()
        ));

        return toResponse(user);
    }

    @Override
    public LoginUserResponse login(LoginUserRequest request) {
        String normalizedEmail = EmailNormalizer.normalize(request.email());

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    normalizedEmail,
                    request.password()
            ));
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmailAndDeletedFalse(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            throw new EmailNotVerifiedException();
        }

        return new LoginUserResponse(tokenService.generateToken(user));
    }

    @Override
    public void verifyEmail(VerifyEmailRequest request) {
        User user = findUserByEmailAndThrow(request.email());

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new EmailAlreadyVerifiedException();
        }

        emailValidationService.validateCode(user.getId(), request.code());
    }

    @Override
    public void resendEmail(ResendEmailRequest request) {
        User user = findUserByEmailAndThrow(request.email());

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new EmailAlreadyExistsException();
        }

        var validation = emailValidationService.createValidation(user);

        sendGridService.sendEmail(new SendGridEmailRequest(
                user.getEmail(),
                user.getFirstName(),
                validation.getVerificationCode()
        ));
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
                user.getDeleted()
        );
    }

    private User findUserByEmailAndThrow(String email) {
        String normalizedEmail = EmailNormalizer.normalize(email);

        return userRepository.findByEmailAndDeletedFalse(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);
    }
}
