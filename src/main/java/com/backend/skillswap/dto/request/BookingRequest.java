package com.backend.skillswap.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {

    @NotNull(message = "SkillRequest ID is required")
    private Long skillRequestId;   // SkillRequest ID jiske against booking ho rahi hai

    private LocalDateTime startTime;   // Session start time
    private LocalDateTime endTime;    // Session end time

    private Integer durationMinutes;
}
