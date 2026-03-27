package com.javacore.spring_api_app.service.email;

import com.javacore.spring_api_app.entity.email.EmailValidation;
import com.javacore.spring_api_app.entity.user.User;
import com.javacore.spring_api_app.exception.custom.BusinessException;
import com.javacore.spring_api_app.repository.email.EmailValidationRepository;
import com.javacore.spring_api_app.repository.user.UserRepository;
import com.javacore.spring_api_app.service.limit.RateLimitService;
import com.javacore.spring_api_app.util.EmailCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
public class EmailValidationServiceImpl implements EmailValidationService {

    private final EmailValidationRepository emailValidationRepository;
    private final UserRepository userRepository;
    private final EmailCodeGenerator codeGenerator;
    private final RateLimitService rateLimitService;

    public EmailValidationServiceImpl(
            EmailValidationRepository emailValidationRepository,
            UserRepository userRepository,
            EmailCodeGenerator codeGenerator,
            RateLimitService rateLimitService) {
        this.emailValidationRepository = emailValidationRepository;
        this.userRepository = userRepository;
        this.codeGenerator = codeGenerator;
        this.rateLimitService = rateLimitService;
    }

    @Override
    public EmailValidation createValidation(User user) {
        var probe = rateLimitService.tryConsume(user.getId());

        if (!probe.isConsumed()) {
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            throw new BusinessException(
                    "Você excedeu o limite de tentativas. Aguarde " + waitSeconds + " segundos para tentar novamente"
            );
        }

        int requestCount = user.getVerificationEmailRequestCount() == null ?
                0 : user.getVerificationEmailRequestCount();

        long delaySeconds = (requestCount + 1) * 120L;

        Instant now = Instant.now();

        if (user.getLastVerificationEmailSentAt() != null &&
                user.getLastVerificationEmailSentAt().isBefore(now.minus(1, ChronoUnit.HOURS))) {
            user.setVerificationEmailRequestCount(0);
        }

        if (user.getLastVerificationEmailSentAt() != null) {
            Instant nextAllowedTime = user.getLastVerificationEmailSentAt().plusSeconds(delaySeconds);

            if (now.isBefore(nextAllowedTime)) {
                long remaining = now.until(nextAllowedTime, ChronoUnit.SECONDS);
                throw new BusinessException("Aguarde " + remaining + " segundos para solicitar um novo email");
            }
        }

        user.setLastVerificationEmailSentAt(now);
        user.setVerificationEmailRequestCount(requestCount + 1);
        userRepository.save(user);

        emailValidationRepository.markAllCodesUsedForUser(user.getId());

        String code = codeGenerator.generateCode();

        EmailValidation emailValidation = EmailValidation.builder()
                .user(user)
                .verificationCode(code)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .used(false)
                .build();
        return emailValidationRepository.save(emailValidation);
    }

    @Override
    public void validateCode(Long userId, String code) {
        EmailValidation validation =
                emailValidationRepository.findByUserIdAndVerificationCode(userId, code)
                    .orElseThrow(() -> new BusinessException("Código inválido"));

        User user = validation.getUser();

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BusinessException("Operação inválida");
        }

        if (validation.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Este código já expirou");
        }

        emailValidationRepository.markAllCodesUsedForUser(userId);

        validation.setUsed(true);
        emailValidationRepository.save(validation);


        user.setEmailVerified(true);
        user.setVerificationEmailRequestCount(0);

        userRepository.save(user);
    }
}