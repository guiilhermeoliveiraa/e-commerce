package com.javacore.spring_api_app.repository.email;

import com.javacore.spring_api_app.entity.email.EmailValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailValidationRepository extends JpaRepository<EmailValidation, Long> {
    Optional<EmailValidation> findByUserIdAndVerificationCode(Long userId, String verificationCode);

    @Modifying
    @Query("UPDATE EmailValidation e SET e.used = true WHERE e.user.id = :userId AND e.used = false")
    int markAllCodesUsedForUser(Long userId);
}
