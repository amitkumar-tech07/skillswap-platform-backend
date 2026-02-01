package com.backend.skillswap.dto.request;

import com.backend.skillswap.entity.enums.SkillCategory;
import com.backend.skillswap.entity.enums.SkillLevel;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSkillRequest {

    @NotBlank(message = "Skill title is required")
    @Size(min = 3, max = 50, message = "Skill title must be between 3 and 50 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    @NotNull(message = "Skill category is required. Ex: MUSIC, PROGRAMMING, LANGUAGE, OTHER")
    private SkillCategory category;

    @NotNull(message = "Skill level is required. Ex: BEGINNER, INTERMEDIATE, EXPERT")
    private SkillLevel level;

    @Min(value = 0, message = "Experience years cannot be negative")
    @Max(value = 50, message = "Experience years cannot exceed 50")
    private int experienceYears;

    @NotNull(message = "Hourly rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Hourly rate must be greater than 0")
    private BigDecimal hourlyRate;
}
