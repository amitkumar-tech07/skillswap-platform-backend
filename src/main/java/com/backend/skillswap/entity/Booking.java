package com.backend.skillswap.entity;

import com.backend.skillswap.entity.enums.BookingStatus;
import com.backend.skillswap.entity.enums.CancelBooking;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "bookings",    // Represents a skill session booking between a requester (learner) and provider (mentor)
        indexes = {
                @Index(name = "idx_booking_provider", columnList = "provider_id"),
                @Index(name = "idx_booking_requester", columnList = "requester_id"),
                @Index(name = "idx_booking_status", columnList = "status")
        }
)
public class Booking {

    // Primary key for booking
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to original skill request triggering this booking --> Link to SkillRequest
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private SkillRequest request;

    // User who wants to learn the skill (learner / student) --> Requester (who wants the skill)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private UserEntity requester;

    // User Provider the skill(who gives the skill)  -->  Skill owner (mentor / service provider)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private UserEntity provider;  // Provider = Skill owner  (Provider = Service provide karne wala)

    // The skill being booked (snapshot of UserSkill)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private UserSkill skill;

    // Booking session start & end time, Note: endTime > startTime   & durationMinutes > 0
    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    // Booking duration in minutes (Example: 60 min session)
    @Column(nullable = false)
    private Integer durationMinutes;    // > 0 (service layer rule)

    // ---- Pricing Snapshot ----
    // Rate per hour snapshot at the time of booking
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal pricePerHour;   // snapshot at booking time (Price of this booking)

    // Calculated total cost for this booking
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    // Current status of the booking (PENDING, CONFIRMED, COMPLETED, CANCELLED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    // reason if user raises a dispute
    @Column(length = 1000)
    private String disputeReason;

    // reason for cancellation
    private String cancelReason;

    // Who cancelled the booking: USER / PROVIDER / ADMIN (only set if status = CANCELLED)
    @Enumerated(EnumType.STRING)
    private CancelBooking cancelledBy;

    // Auto-managed timestamps for creation & last update
    @CreationTimestamp
    private LocalDateTime createdAt;    //  Auto set when booking created

    @UpdateTimestamp
    private LocalDateTime updatedAt;    //  Auto update when booking updated

}

