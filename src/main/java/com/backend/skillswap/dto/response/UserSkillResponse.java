package com.backend.skillswap.dto.response;

import com.backend.skillswap.entity.enums.SkillCategory;
import com.backend.skillswap.entity.enums.SkillLevel;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSkillResponse {

    private Long id;
    private Long userId;

    private String title;
    private String description;

    private SkillCategory category;
    private SkillLevel level;

    private int experienceYears;
    private BigDecimal hourlyRate;

    private boolean verified;
    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
