package com.backend.skillswap.service;

import com.backend.skillswap.dto.request.LoginRequest;
import com.backend.skillswap.dto.request.RefreshTokenRequest;
import com.backend.skillswap.dto.request.RegisterRequest;
import com.backend.skillswap.dto.response.AuthResponse;
import com.backend.skillswap.entity.UserEntity;

public interface AuthService {

    AuthResponse register(RegisterRequest request);           // user signup

    AuthResponse login(LoginRequest request);                 // login with email/username

    AuthResponse refreshToken(RefreshTokenRequest request);   // rotate refresh token

    String logout(String refreshToken);                          // logout user

    String verifyOtp(String email, String otp);                // email OTP verification

    String resendOtp(String email);                            // resend verification OTP

    Long getCurrentUserId();                                   // fetch logged-in user id

    String verifyEmailToken(String token);                     // email link verification

    void approveProviderRole(Long userId);

    void removeProviderRole(Long userId);

    UserEntity getCurrentUser();

    UserEntity getUserById(Long userId);

}
