package com.backend.skillswap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "email_otp")
public class EmailOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime expiryTime;   // OTP expiration time

    // OTP brute-force protection
    @Builder.Default
    private int attemptCount = 0;         // number of failed OTP attempts
    private LocalDateTime blockedUntil;   // OTP verification blocked until this time

    private LocalDateTime lastSentAt;   // last OTP sent timestamp (resend rate limiting)

}

