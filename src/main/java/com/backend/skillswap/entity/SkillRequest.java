package com.backend.skillswap.entity;

import com.backend.skillswap.entity.enums.SkillRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "skill_requests",
        indexes = {
                @Index(name = "idx_request_sender", columnList = "sender_id"),
                @Index(name = "idx_request_receiver", columnList = "receiver_id"),
                @Index(name = "idx_request_skill", columnList = "skill_id"),
                @Index(name = "idx_request_status", columnList = "status")
        }
)
public class SkillRequest {

    // Primary key for skill request
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Logged-in Learner who is requesting to learn the skill --> Request sender (learner)
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    // Skill provider who will accept or reject the request -->  Request receiver (skill provider)
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver;

    // Specific skill for which the request is sent
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private UserSkill skill;

    // Optional message or introduction sent by the learner
    @Column(length = 500)
    private String message;

    // Current lifecycle status of the request (PENDING / ACCEPTED / REJECTED / CANCELLED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SkillRequestStatus status = SkillRequestStatus.PENDING;

    // Auto-expiry time after which the request becomes invalid
    private LocalDateTime expiresAt;

    // Timestamp when the skill request was created (Auto set when booking created)
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Timestamp when the request status or details were last updated (Auto update when booking updated)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

