package com.backend.skillswap.dto.response;

import com.backend.skillswap.entity.enums.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {

    private Long bookingId;

    // Skill info
    private Long skillId;
    private String skillName;

    // User info
    private Long requesterId;
    private Long providerId;
    private String providerName;

    // Session info
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;

    // Pricing snapshot
    private BigDecimal pricePerHour;
    private BigDecimal totalAmount;

    // Status info
    private BookingStatus status;
    private String cancelReason;

    private String message;   // API clarity (imp)

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
