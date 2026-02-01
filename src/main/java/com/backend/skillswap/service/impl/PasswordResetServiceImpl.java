package com.backend.skillswap.service.impl;

import com.backend.skillswap.entity.PasswordResetOtp;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.exception.authentication.InvalidCredentialsException;
import com.backend.skillswap.exception.authentication.OtpExpiredException;
import com.backend.skillswap.repository.PasswordResetOtpRepository;
import com.backend.skillswap.repository.RefreshTokenRepository; // For refresh token invalidation
import com.backend.skillswap.repository.UserRepository;
import com.backend.skillswap.service.EmailService;
import com.backend.skillswap.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;


@RequiredArgsConstructor
@Transactional
@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetOtpRepository otpRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // SecureRandom is cryptographically safe for OTP generation
    private final SecureRandom random = new SecureRandom();

    // GENERATE 6-DIGIT OTP
    private String generateOtp() {
        return String.valueOf(100000 + random.nextInt(900000));
    }

    // ================= SEND RESET OTP ======================
    @Override
    public String sendResetOtp(String email) {

        // Case-insensitive fetch of user
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new InvalidCredentialsException("Email not registered"));

        // Fetch existing OTP (unique per email)
        PasswordResetOtp existing = otpRepository.findByEmail(email).orElse(null);

        if (existing != null) {
            // ===== RATE LIMITING =====
            // 1-minute cooldown between OTP sends
            if (existing.getLastSentTime() != null &&
                    existing.getLastSentTime().isAfter(LocalDateTime.now().minusMinutes(1))) {
                throw new InvalidCredentialsException("Please wait 1 minute before requesting a new OTP");
            }

            // Reset resend count if OTP older than 1 hour
            if (existing.getCreatedAt().isBefore(LocalDateTime.now().minusHours(1))) {
                existing.setResendCount(0);
                existing.setCreatedAt(LocalDateTime.now());
            }

            // Max 3 resends per hour
            if (existing.getResendCount() >= 3) {
                throw new InvalidCredentialsException("OTP request limit exceeded, try after 1 hour");
            }

            // Generate new OTP & update existing record
            String otp = generateOtp();
            existing.setOtp(otp);
            existing.setExpiryTime(LocalDateTime.now().plusMinutes(10));
            existing.setResendCount(existing.getResendCount() + 1);
            existing.setLastSentTime(LocalDateTime.now());
            existing.setAttemptCount(0); // reset wrong attempts
            existing.setVerified(false); // mark unverified
            otpRepository.save(existing);

            emailService.sendPasswordResetOtpEmail(email, otp);
            return "Password reset OTP resent successfully";
        }

        // No existing OTP → create new (unique per email)
        String otp = generateOtp();
        PasswordResetOtp resetOtp = PasswordResetOtp.builder()
                .email(email)
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(10))
                .attemptCount(0)
                .resendCount(1)
                .lastSentTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .verified(false)
                .build();

        otpRepository.save(resetOtp);
        emailService.sendPasswordResetOtpEmail(email, otp);

        return "Password reset OTP sent successfully";
    }

    // ================= VERIFY RESET OTP ======================
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    @Override
    public String verifyResetOtp(String email, String otp) {

        otp = otp.trim(); // VERY IMPORTANT

        // Fetch OTP entity by email
        PasswordResetOtp resetOtp = otpRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("No OTP found"));

        LocalDateTime now = LocalDateTime.now();

        // Check expiry
        if (resetOtp.getExpiryTime().isBefore(now))
            throw new OtpExpiredException("OTP expired");

        // Brute-force protection → block OTP after 5 wrong attempts
        if (resetOtp.getAttemptCount() >= 5)
            throw new InvalidCredentialsException("Too many wrong attempts, OTP blocked");

        // Wrong OTP → increment attempt count
        if (!resetOtp.getOtp().trim().equals(otp)) {
            int attempts = resetOtp.getAttemptCount() + 1;
            resetOtp.setAttemptCount(attempts);
            otpRepository.save(resetOtp);

            // Block immediately on 5th wrong attempt --> Wrong OTP entered 5 times means On 5th attempt → "OTP blocked"  --> 6th attempt never allowed
            if (attempts >= 5) {
                throw new InvalidCredentialsException("Too many wrong attempts, OTP blocked");
            }

            throw new InvalidCredentialsException("Invalid OTP");
        }

        // Correct OTP → mark verified
        resetOtp.setVerified(true);
        otpRepository.save(resetOtp);

        return "OTP verified successfully";
    }

    // ========= RESET PASSWORD ========= Note: Password reset invalidates all refresh tokens to log out every active session ======
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    @Override
    public String resetPassword(String email, String newPassword, String confirmedNewPassword) {

        // Check new password matches confirmation
        if (!newPassword.equals(confirmedNewPassword))
            throw new InvalidCredentialsException("New password and confirmation do not match");

        // Fetch user by email
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new InvalidCredentialsException("Email not valid"));

        // Fetch OTP entity
        PasswordResetOtp resetOtp = otpRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("OTP missing"));

        // Ensure OTP is verified
        if (!resetOtp.isVerified())
            throw new InvalidCredentialsException("OTP not verified");

        // Password validation: minimum 8 characters, letters & numbers
        if (newPassword == null || !newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            throw new InvalidCredentialsException(
                    "Password invalid: minimum 8 characters, must include letters and numbers"
            );
        }

        // Encode & save new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate all refresh tokens for this user
        refreshTokenRepository.deleteByUser(user);

        // Clean up OTP record after successful reset
        otpRepository.deleteByEmail(email);

        // Notify user about password reset
        emailService.sendPasswordResetConfirmation(user.getEmail());

        return "Password reset successfully";
    }
}
