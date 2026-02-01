package com.backend.skillswap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_profile")
@EntityListeners(AuditingEntityListener.class)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // UserProfile owns the relationship (user_id is stored here)
    // If the user is deleted, the profile is deleted automatically
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;    // 1 user → 1 profile

    @NotBlank(message = "First name cannot be blank!")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name cannot be blank!")
    @Column(nullable = false)
    private String lastName;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String bio;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false)
    private String profileImage;   // store URL or filename

    @Column(nullable = false, length = 100)
    private String location;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Derived field: combines first and last name (not persisted in DB)
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

}


