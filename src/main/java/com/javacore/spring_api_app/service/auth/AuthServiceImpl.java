package com.javacore.spring_api_app.service.auth;

import com.javacore.spring_api_app.domain.email.EmailNormalizer;
import com.javacore.spring_api_app.domain.name.NameNormalizer;
import com.javacore.spring_api_app.dto.request.RegisterUserRequest;
import com.javacore.spring_api_app.dto.response.RegisterUserResponse;
import com.javacore.spring_api_app.entity.User;
import com.javacore.spring_api_app.exception.custom.BusinessException;
import com.javacore.spring_api_app.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public RegisterUserResponse register(RegisterUserRequest request) {
        String normalizedFirstName = NameNormalizer.normalize(request.firstName());
        String normalizedLastName = NameNormalizer.normalize(request.lastName());
        String normalizedEmail = EmailNormalizer.normalize(request.email());

        if (userRepository.existsByEmailAndDeletedFalse(normalizedEmail)) {
            throw new BusinessException("Operação inválida");
        }

        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException("Senhas não coincidem");
        }

        User user = User.builder()
                .email(normalizedEmail)
                .firstName(normalizedFirstName)
                .lastName(normalizedLastName)
                .password(passwordEncoder.encode(request.password()))
                .build();

        return toResponse(userRepository.save(user));
    }

    private RegisterUserResponse toResponse(User user) {
        return new RegisterUserResponse(
                user.getPublicId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getDeleted()
        );
    }
}
