package com.backend.skillswap.security.JWT;

import com.backend.skillswap.entity.UserEntity;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtUtil {

    // Extract username/email from token(JWT)
    String extractUsername(String token);

    // Validate token with UserDetails
    boolean isTokenValid(String token, UserDetails userDetails);

    // Check token expiry only
    boolean isTokenExpired(String token);

    // Extract userId claim from JWT/Token
    Long getUserIdFromToken(String token);

    // Access token expiry (ms) for response
    long getAccessTokenExpiryInMs();

    // Generate JWT access token (Generate short-lived access token)
    String generateAccessToken(UserEntity user);

}
