package com.javacore.spring_api_app.service.email;

import com.javacore.spring_api_app.entity.email.EmailValidation;
import com.javacore.spring_api_app.entity.user.User;
import com.javacore.spring_api_app.exception.custom.EmailNotVerifiedException;
import com.javacore.spring_api_app.exception.custom.InvalidVerificationCodeException;
import com.javacore.spring_api_app.exception.custom.RateLimitExceededException;
import com.javacore.spring_api_app.repository.email.EmailValidationRepository;
import com.javacore.spring_api_app.repository.user.UserRepository;
import com.javacore.spring_api_app.service.limit.RateLimitService;
import com.javacore.spring_api_app.util.EmailCodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
@Slf4j
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
        log.debug("event=verification_code_generation_attempt userPublicId={}", user.getPublicId());

        var probe = rateLimitService.tryConsume(user.getId());

        if (!probe.isConsumed()) {
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            log.warn("event=verification_code_generation_failed reason=rate_limit_exceeded userPublicId={} waitSeconds={}",
                    user.getPublicId(), waitSeconds);
            throw new RateLimitExceededException(waitSeconds);
        }

        int requestCount = user.getVerificationEmailRequestCount() == null ?
                0 : user.getVerificationEmailRequestCount();

        long delaySeconds = (requestCount + 1) * 120L;

        Instant now = Instant.now();

        if (user.getLastVerificationEmailSentAt() != null &&
                user.getLastVerificationEmailSentAt().isBefore(now.minus(1, ChronoUnit.HOURS))) {
            log.debug("event=verification_request_counter_reset userPublicId={}", user.getPublicId());
            user.setVerificationEmailRequestCount(0);
        }

        if (user.getLastVerificationEmailSentAt() != null) {
            Instant nextAllowedTime = user.getLastVerificationEmailSentAt().plusSeconds(delaySeconds);

            if (now.isBefore(nextAllowedTime)) {
                long remaining = now.until(nextAllowedTime, ChronoUnit.SECONDS);
                log.warn("event=verification_code_generation_failed reason=too_early_request userPublicId={} remainingSeconds={}",
                        user.getPublicId(), remaining);
                throw new RateLimitExceededException(remaining);
            }
        }

        user.setLastVerificationEmailSentAt(now);
        user.setVerificationEmailRequestCount(requestCount + 1);
        userRepository.save(user);

        log.debug("event=verification_request_updated userPublicId={} requestCount={}",
                user.getPublicId(), user.getVerificationEmailRequestCount());

        emailValidationRepository.markAllCodesUsedForUser(user.getId());

        String code = codeGenerator.generateCode();

        EmailValidation emailValidation = EmailValidation.builder()
                .user(user)
                .verificationCode(code)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .used(false)
                .build();

        EmailValidation saved = emailValidationRepository.save(emailValidation);

        log.info("event=verification_code_generated userPublicId={} expiresAt={}",
                user.getPublicId(), saved.getExpiresAt());

        return saved;
    }

    @Override
    public void validateCode(Long userId, String code) {
        log.debug("event=verification_code_validation_attempt userId={}", userId);

        EmailValidation validation =
                emailValidationRepository.findByUserIdAndVerificationCode(userId, code)
                    .orElseThrow(() -> {
                        log.warn("event=verification_code_validation_failed reason=invalid_code userId={}", userId);
                        return new InvalidVerificationCodeException();
                    });

        User user = validation.getUser();

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            log.warn("event=verification_code_validation_failed reason=already_verified userPublicId={}",
                    user.getPublicId());
            throw new EmailNotVerifiedException();
        }

        if (validation.getExpiresAt().isBefore(Instant.now())) {
            log.warn("event=verification_code_validation_failed reason=already_verified userPublicId={}",
                    user.getPublicId());
            throw new InvalidVerificationCodeException();
        }

        emailValidationRepository.markAllCodesUsedForUser(userId);

        validation.setUsed(true);
        emailValidationRepository.save(validation);


        user.setEmailVerified(true);
        user.setVerificationEmailRequestCount(0);

        userRepository.save(user);
        log.info("event=email_verification_success userPublicId={}", user.getPublicId());
    }
}