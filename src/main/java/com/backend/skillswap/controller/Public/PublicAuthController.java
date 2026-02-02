package com.backend.skillswap.controller.Public;

import com.backend.skillswap.config.OpenApiConfig;
import com.backend.skillswap.dto.request.LoginRequest;
import com.backend.skillswap.dto.request.RefreshTokenRequest;
import com.backend.skillswap.dto.request.RegisterRequest;
import com.backend.skillswap.dto.response.AuthResponse;
import com.backend.skillswap.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(
        name = "01. Public Authentication & Authorization APIs",
        description = """
Handles complete authentication lifecycle:

• New User Registration
• Email OTP Verification
• Login & JWT Token Generation
• Refresh Token Handling
• Secure Logout
• Resend OTP for verification
"""
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class PublicAuthController {

    private final AuthService authService;

    // ========================= REGISTER ========================= //
    @Operation(
            summary = "Register New User (Email OTP Based)",
            description = """
Creates a new user account.

Flow:
1️⃣ User registers
2️⃣ System generates OTP
3️⃣ OTP emailed to user
4️⃣ User must verify OTP to activate account
"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully & OTP sent"),
            @ApiResponse(responseCode = "409", description = "Email or username already exists"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Delegates registration & OTP sending to AuthService
        return ResponseEntity.ok(authService.register(request));
    }

    // ========================= LOGIN ============================ //
    @Operation(
            summary = "Login User & Generate JWT Tokens",
            description = """
Authenticates user using email or username + password.

Returns:
✔️ Access Token (Short-lived, ~15 min)
✔️ Refresh Token (Long-lived, ~7 days)

Conditions:
- Email must be verified
"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or email not verified")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ========================= REFRESH TOKEN ==================== //
    @Operation(
            summary = "Generate New Access Token using Refresh Token",
            description = """
Uses a valid Refresh Token to generate a new Access Token.

Use case:
✔️ When Access Token expires
❌ Do not login again
"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    // ========================= LOGOUT =========================== //
    @Operation(
            summary = "Logout User",
            description = """
Securely logs out the authenticated user.

Workflow:
• Validates provided refresh token
• Deletes refresh token from DB
• User cannot generate new access token
• Terminates the authenticated session
""",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok("Logged out successfully");
    }

    // ========================= VERIFY OTP ======================= //
    @Operation(
            summary = "Verify Email OTP",
            description = """
Verifies the OTP sent to user email & activates the account.
"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        return ResponseEntity.ok(authService.verifyOtp(email, otp));
    }

    // ========================= VERIFY EMAIL TOKEN ================ //
    @Operation(
            summary = "Verify Email Token",
            description = "Verifies the user's email using a token sent via email. Token valid 15 minutes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        authService.verifyEmailToken(token);
        return ResponseEntity.ok("Email verified successfully! You can now login.");
    }

    // ========================= RESEND OTP ======================= //
    @Operation(
            summary = "Resend Email OTP",
            description = "Sends a new OTP to the registered email for verification."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP resent successfully"),
            @ApiResponse(responseCode = "404", description = "Email not found"),
            @ApiResponse(responseCode = "400", description = "Too many requests, wait before retrying")
    })
    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestParam String email) {
        return ResponseEntity.ok(authService.resendOtp(email));
    }
}
