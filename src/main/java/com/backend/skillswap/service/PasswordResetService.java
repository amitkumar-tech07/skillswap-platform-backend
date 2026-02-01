package com.backend.skillswap.service;

public interface PasswordResetService {

    // Send OTP to user's email for password reset
    String sendResetOtp(String email);

    // Verify the OTP entered by the user
    String verifyResetOtp(String email, String otp);

    // Reset the user's password after OTP verification
    String resetPassword(String email, String newPassword, String confirmedNewPassword);

}
