package com.backend.skillswap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SkillRequestRequest {

    @NotNull(message = "Skill ID is required")
    private Long skillId;     // Kis skill ke liye request


    @NotBlank(message ="Message must be at most 500 characters")
    @Size(min = 2, max = 500)
    private String message;
}
