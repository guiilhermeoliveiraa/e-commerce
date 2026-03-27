package com.javacore.spring_api_app.entity.email;

import com.javacore.spring_api_app.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "email_validations")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EmailValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String verificationCode;

    private Instant expiresAt;

    private Boolean used;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}