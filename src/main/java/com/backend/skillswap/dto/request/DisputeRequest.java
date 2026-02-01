package com.backend.skillswap.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisputeRequest {

    @NotBlank(message = "Dispute reason is required")
    private String reason;
}
