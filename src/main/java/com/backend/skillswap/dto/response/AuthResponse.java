package com.backend.skillswap.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;

    private Long userId;
    private String username;
    private String email;
    private List<String> roles;   // Multiple Roles Support

    // these two fields for OTP messages
    private boolean otpSent;
    private String message;

}



