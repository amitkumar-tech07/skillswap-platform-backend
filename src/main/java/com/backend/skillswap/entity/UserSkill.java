package com.backend.skillswap.entity;

import com.backend.skillswap.entity.enums.SkillCategory;
import com.backend.skillswap.entity.enums.SkillLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "skills",
        indexes = {
                @Index(name = "idx_skill_user", columnList = "user_id"),
                @Index(name = "idx_skill_category", columnList = "category"),
                @Index(name = "idx_skill_active", columnList = "is_active")
        }
)
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Skill belongs to exactly one user (provider)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)    // Skill → ManyToOne(User)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;   // Ye kis user ki skill hai

    // Skill title/Name like : Java, React, DSA, Guitar etc
    @Column(nullable = false, length = 100)
    private String title;

    // Optional description
    @Column(length = 500)
    private String description;

    // Skill category like : PROGRAMMING, MUSIC, FITNESS , OTHER
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillCategory category;

    // Skill level: BEGINNER / INTERMEDIATE / EXPERT
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillLevel level;

    // User Experience in years
    @Min(0)
    @Column(nullable = false)
    private int experienceYears;

    // Hourly rate charged for this skill (wallet / escrow based)
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    // Admin verification skill flag to prevent fake or low-quality skills (only verified skills are publicly visible)
    @Builder.Default
    @Column(nullable = false)
    private boolean verified = false;

    // Soft delete flag (inactive skills are hidden but not removed)
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // Audit timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

}
