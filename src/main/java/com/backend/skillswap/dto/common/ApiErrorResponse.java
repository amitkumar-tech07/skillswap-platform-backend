package com.backend.skillswap.dto.common;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiErrorResponse {  // Standardized API error response structure.

    private Instant timestamp;
    private int status;
    private String errorCode;
    private String message;
    private String path;

}