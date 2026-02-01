package com.backend.skillswap.service.impl;

import com.backend.skillswap.entity.enums.Role;
import com.backend.skillswap.exception.authentication.*;
import com.backend.skillswap.exception.common.ResourceNotFoundException;
import com.backend.skillswap.security.CustomUserDetails;
import com.backend.skillswap.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.backend.skillswap.dto.request.LoginRequest;
import com.backend.skillswap.dto.request.RefreshTokenRequest;
import com.backend.skillswap.dto.request.RegisterRequest;
import com.backend.skillswap.dto.response.AuthResponse;
import com.backend.skillswap.entity.*;
import com.backend.skillswap.mapper.UserMapper;
import com.backend.skillswap.repository.*;
import com.backend.skillswap.security.JWT.JwtUtil;
import com.backend.skillswap.service.EmailService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final UserProfileRepository userProfileRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Secure OTP generator (never use Math.random)
    private final SecureRandom random = new SecureRandom();

    // ======================= REGISTER / SIGNUP =======================
    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {
        try {
            // Normalize email & username (trim + lowercase for uniqueness)
            String email = request.getEmail().trim().toLowerCase();
            String username = request.getUsername().trim().toLowerCase();

            request.setEmail(email);
            request.setUsername(username);

            // Username validation (3–50 chars, no spaces, only letters, numbers, _ and .)
            if (!username.matches("^[a-zA-Z0-9._]{3,50}$")) {
                throw new InvalidCredentialsException(
                        "Username must be 3–50 characters long and can contain only letters, numbers, underscore (_) and dot (.) with no spaces");
            }

            // Duplicate checks (email & username)
            boolean emailExists = userRepository.findByEmailIgnoreCase(email).isPresent();
            boolean usernameExists = userRepository.findByUsernameIgnoreCase(username).isPresent();

            if (emailExists && usernameExists) {
                throw new DuplicateUserException("Email and Username both already taken");
            }

            if (usernameExists) {
                throw new DuplicateUsernameException("Username already taken");
            }

            if (emailExists) {
                throw new DuplicateEmailException("Email already exists");
            }

            // Strong Password validation (must be at least 8 chars)
            if (!request.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
                throw new InvalidCredentialsException("Password must be at least 8 characters and contain letters & numbers");
            }

            // ROLE ASSIGNMENT  (default USER if none provided)
            List<Role> finalRoles = List.of(Role.USER);   // 🔐 ALWAYS USER

            // Map DTO → Entity
            UserEntity user = UserMapper.toEntity(request, finalRoles);

            // Set defaults & mandatory fields
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setActive(true);
            user.setEmailVerified(false);
            user.setTimeCredits(0);                         // DB NOT NULL fix
            user.setWalletBalance(BigDecimal.ZERO);   // DB NOT NULL fix

            // Save/Persist user in DB
            UserEntity savedUser = userRepository.save(user);

            // Create fallback email verification token
            emailVerificationTokenRepository.save(
                    EmailVerificationToken.builder()
                            .token(UUID.randomUUID().toString())
                            .user(savedUser)
                            .expiryDate(LocalDateTime.now().plusMinutes(15))
                            .build()
            );

            // ================= OTP FLOW =================

            // Remove old OTPs for this email
            emailOtpRepository.deleteByEmail(email); // cleanup old OTPs

            // Generate a secure 6-digit OTP
            String otp = String.valueOf(100000 + random.nextInt(900000));

            // Save OTP in DB
            emailOtpRepository.save(
                    EmailOtp.builder()
                            .email(email)
                            .otp(otp)
                            .expiryTime(LocalDateTime.now().plusMinutes(10))
                            .attemptCount(0)
                            .blockedUntil(null)
                            .lastSentAt(LocalDateTime.now())
                            .build()
            );

            // Send OTP email & capture success/failure
            boolean otpSent;
            try {
                otpSent = emailService.sendVerificationOtpEmail(email, otp).get(); // wait for async completion
            } catch (Exception e) {
                otpSent = false;
                log.error("Failed to send OTP to {}", email, e);
            }

            // Build response
            return AuthResponse.builder()
                    .accessToken("") // JWT blocked until email verified  (JWT not issued until email verified)
                    .refreshToken("")
                    .tokenType("Bearer")
                    .expiresIn(0)
                    .userId(savedUser.getId())
                    .username(savedUser.getUsername())
                    .email(savedUser.getEmail())
                    .roles(savedUser.getRoles().stream().map(Role::name).toList())
                    .otpSent(otpSent)
                    .message(otpSent
                            ? "Registration successful! OTP has been sent to your email."
                            : "Registration successful! OTP could not be sent. Please request again.")
                    .build();

        } catch (DuplicateEmailException
                 | DuplicateUsernameException
                 | DuplicateUserException
                 | InvalidCredentialsException e) {
            throw e;
        }
        // Unexpected exceptions → hide internal details
        catch (Exception e) {
            log.error("Registration failed for email={}", request.getEmail(), e);
            throw new InvalidCredentialsException("Registration failed. Try again.");
        }
    }

    // ======================= LOGIN =======================
    @Transactional
    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            // Normalize loginId (email / username)
            String loginId = request.getLoginId().trim().toLowerCase();

            // Fetch user by email or username
            UserEntity user = userRepository
                    .findByEmailIgnoreCaseOrUsernameIgnoreCase(loginId, loginId)
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

            // Password verification (BCrypt) / Verify password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
                throw new InvalidCredentialsException("Invalid credentials");

            //  Email verification check / Block login until email verified
            if (!user.isEmailVerified())
                throw new EmailNotVerifiedException("Email not verified");

            // Generate access token (JWT) &  refresh token
            String accessToken = jwtUtil.generateAccessToken(user);

            //  Enforce single active session
            refreshTokenRepository.deleteByUser(user);

            // Generate & Save refresh token
            String refreshToken = UUID.randomUUID().toString();
            refreshTokenRepository.save(
                    RefreshToken.builder()
                            .token(refreshToken)
                            .user(user)
                            .expiryDate(LocalDateTime.now().plusDays(7))
                            .build()
            );

            // Build login response with multiple roles + success message
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getAccessTokenExpiryInMs())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(user.getRoles().stream().map(Role::name).toList())
                    .otpSent(false)   // login doesn't send OTP
                    .message("Login successful")
                    .build();

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidCredentialsException("Login failed");
        }
    }

    // ======================= REFRESH TOKEN =======================
    @Transactional
    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        try {
            // Validate incoming refresh token
            RefreshToken refreshToken = refreshTokenRepository
                    .findByToken(request.getRefreshToken())
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

            // Check expiry
            if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                refreshTokenRepository.delete(refreshToken); // cleanup
                throw new TokenExpiredException("Refresh token expired");
            }

            // Get user from refresh token
            UserEntity user = refreshToken.getUser();

            // Rotate refresh token (IMP)
            // delete + insert & update same DB row
            String newRefreshToken = UUID.randomUUID().toString();

            refreshToken.setToken(newRefreshToken);
            refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));

            refreshTokenRepository.save(refreshToken);

            // Generate new access token (JWT)
            String newAccessToken = jwtUtil.generateAccessToken(user);

            // Build response
            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getAccessTokenExpiryInMs())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(
                            user.getRoles()
                                    .stream()
                                    .map(Role::name)
                                    .toList()
                    )
                    .otpSent(false)
                    .message("Token refreshed successfully")
                    .build();

        } catch (InvalidCredentialsException | TokenExpiredException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidCredentialsException("Refresh failed");
        }
    }

    // ======================= LOGOUT =======================
    @Transactional
    @Override
    public String logout(String refreshToken) {

        try {
            // Validate input
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new InvalidCredentialsException("Refresh token is required");
            }

            // Fetch refresh token (STRICT)
            RefreshToken token = refreshTokenRepository
                    .findByToken(refreshToken)
                    .orElseThrow(() ->
                            new InvalidCredentialsException("Invalid or expired refresh token")
                    );

            //  Delete refresh token (REAL logout)
            refreshTokenRepository.delete(token);

            // Success
            return "Logged out successfully";

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Logout failed for refreshToken={}", refreshToken, e);
            throw new InvalidCredentialsException("Logout failed");
        }
    }


    // ======================= VERIFY OTP =======================
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    @Override
    public String verifyOtp(String email, String otp) {
        try {

            // Normalize input OTP
            otp = otp.trim();

            EmailOtp emailOtp = emailOtpRepository.findByEmail(email)
                    .orElseThrow(() -> new InvalidCredentialsException("OTP not found"));

            LocalDateTime now = LocalDateTime.now();

            //  Blocked OTP due to excessive attempts
            if (emailOtp.getBlockedUntil() != null && emailOtp.getBlockedUntil().isAfter(now))
                throw new OtpBlockedException("Too many attempts. Try later.");

            // OTP expiry check
            if (emailOtp.getExpiryTime().isBefore(now))
                throw new OtpExpiredException("OTP expired");

            // OTP mismatch handling
            if (!emailOtp.getOtp().trim().equals(otp)) {

                int attempts = emailOtp.getAttemptCount() + 1;
                emailOtp.setAttemptCount(attempts);

                if (attempts >= 5)
                    emailOtp.setBlockedUntil(now.plusMinutes(15));

                emailOtpRepository.save(emailOtp);
                throw new InvalidCredentialsException("Invalid OTP");
            }

            // Fetch user & mark email verified (SUCCESS)
            UserEntity user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new InvalidCredentialsException("User not found"));

            // Centralized email verification & profile creation
            verifyEmailAndCreateProfile(user);  // mark email verified

            // Cleanup OTP after success
            emailOtpRepository.deleteByEmail(email);

            return "Email verified successfully & profile created";

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidCredentialsException("OTP verification failed");
        }
    }

    // ======================= VERIFY EMAIL TOKEN =======================
    @Transactional
    @Override
    public String verifyEmailToken(String token) {
        try {
            EmailVerificationToken verifyToken =
                    emailVerificationTokenRepository.findByToken(token)
                            .orElseThrow(() -> new InvalidCredentialsException("Invalid token"));

            // Expiry validation
            if (verifyToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                emailVerificationTokenRepository.delete(verifyToken);
                throw new InvalidCredentialsException("Token expired");
            }

            UserEntity user = verifyToken.getUser();

            // Centralized email verification & profile creation
            verifyEmailAndCreateProfile(user);

            // Cleanup token
            emailVerificationTokenRepository.delete(verifyToken);

            return "Email verified successfully & profile created";

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidCredentialsException("Email verification failed");
        }
    }

    // ======================= PRIVATE HELPER =======================

    // Marks user email as verified and creates default profile if it doesn't exist.
    // Centralized email verification + profile creation
    private void verifyEmailAndCreateProfile(UserEntity user) {

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        if (!userProfileRepository.existsByUser_Id(user.getId())) {
            userProfileRepository.save(
                    UserProfile.builder()
                            .user(user)
                            .firstName(user.getUsername())
                            .lastName("kumar")
                            .bio("SkillSwap User")
                            .country("India")
                            .location("India")
                            .profileImage("https://res.cloudinary.com/dijfnm4na/image/upload/v1766194813/profile_d04eth.jpg")
                            .build()
            );
        }
    }

    // ======================= RESEND OTP =======================
    @Transactional
    @Override
    public String resendOtp(String email) {
        try {
            UserEntity user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new InvalidCredentialsException("User not found"));

            if (user.isEmailVerified())
                throw new InvalidCredentialsException("Email already verified");

            LocalDateTime now = LocalDateTime.now();
            EmailOtp oldOtp = emailOtpRepository.findByEmail(email).orElse(null);

            if (oldOtp != null && oldOtp.getLastSentAt() != null) {
                long seconds = Math.max(0, java.time.Duration.between(oldOtp.getLastSentAt(), now).getSeconds());
                if (seconds < 60)
                    throw new InvalidCredentialsException("Wait " + (60 - seconds) + " seconds before resend");
            }

            emailOtpRepository.deleteByEmail(email);

            String otp = String.valueOf(100000 + random.nextInt(900000));

            emailOtpRepository.save(
                    EmailOtp.builder()
                            .email(email)
                            .otp(otp)
                            .expiryTime(now.plusMinutes(10))
                            .attemptCount(0)
                            .blockedUntil(null)
                            .lastSentAt(now)
                            .build()
            );

            emailService.sendVerificationOtpEmail(email, otp);

            return "OTP resent successfully";

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidCredentialsException("OTP resend failed");
        }
    }

    // ======================= CURRENT USER =======================
    @Override
    public Long getCurrentUserId() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUser().getId();
        }
        throw new InvalidCredentialsException("Unauthenticated");
    }

    //  Admin USER ko  → PROVIDER bna sakta h
    @Transactional
    @Override
    public void approveProviderRole(Long userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // already provider hai?
        if (user.getRoles().contains(Role.PROVIDER)) {
            throw new InvalidCredentialsException("User is already a PROVIDER");
        }

        // USER → PROVIDER
        user.getRoles().add(Role.PROVIDER);

        userRepository.save(user);
    }

    // Remove Provider Role means ADMIN PROVIDER role ko remove kar sake (PROVIDER → USER/normal).
    @Transactional
    @Override
    public void removeProviderRole(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user actually has PROVIDER role
        if (!user.getRoles().contains(Role.PROVIDER)) {
            throw new InvalidCredentialsException("User does not have PROVIDER role");
        }

        // Remove PROVIDER role
        user.getRoles().remove(Role.PROVIDER);

        //  ensure USER role exists after removal
        if (!user.getRoles().contains(Role.USER)) {
            user.getRoles().add(Role.USER);
        }

        userRepository.save(user);
    }

    @Override
    public UserEntity getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public UserEntity getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
