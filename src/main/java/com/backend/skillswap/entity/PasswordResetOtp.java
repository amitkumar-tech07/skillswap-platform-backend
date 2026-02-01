package com.backend.skillswap.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "password_reset_otp")
public class PasswordResetOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    @Column(nullable = false)
    private int attemptCount;      // wrong OTP tries count

    @Column(nullable = false)
    private int resendCount;       // resend limit

    private LocalDateTime lastSentTime;

    @Column(nullable = false)
    private boolean verified = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

}
