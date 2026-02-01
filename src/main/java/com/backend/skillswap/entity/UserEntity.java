package com.backend.skillswap.entity;

import com.backend.skillswap.entity.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true,length = 50)
    @NotBlank(message = "username cannot be blank!")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false, length = 255)
    private String password;

    // Users can be assigned multiple roles
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")       // Create a new table in the database : user_roles -> user_id | role
    )
    @Column(name = "role",nullable = false, length = 25)
    private List<Role> roles;

    // Account Status Flags
    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal walletBalance = BigDecimal.ZERO;

    // Time credits represent skill-exchange currency (future feature)
    // 1 credit = 1 hour of skill provided.
    // Users earn credits by teaching & spend credits by learning.
    @Column(nullable = false)
    private Integer timeCredits = 0;

    @Column(nullable = false)
    private boolean emailVerified = false;    // Account email Status Flags

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Holds extended profile information separate from authentication data
    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
    private UserProfile userProfile;

}
