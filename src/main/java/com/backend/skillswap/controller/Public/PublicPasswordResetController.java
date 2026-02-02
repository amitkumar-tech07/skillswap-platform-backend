package com.backend.skillswap.controller.Public;

import com.backend.skillswap.dto.request.ForgotPasswordRequest;
import com.backend.skillswap.dto.request.ResetPasswordRequest;
import com.backend.skillswap.dto.request.VerifyResetOtpRequest;
import com.backend.skillswap.dto.common.ApiMessageResponse;
import com.backend.skillswap.service.PasswordResetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(
        name = "02. Public Password Reset & Account Recovery APIs",
        description = "Secure APIs for password reset using email OTP verification"
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class PublicPasswordResetController {

    private final PasswordResetService passwordResetService;

    // ===================== FORGOT PASSWORD =====================
    @Operation(summary = "Send password reset OTP")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OTP sent successfully",
                    content = @Content(schema = @Schema(implementation = ApiMessageResponse.class))
            )
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiMessageResponse> forgotPassword(
            @RequestBody ForgotPasswordRequest request
    ) {
        return ResponseEntity.ok(
                new ApiMessageResponse(
                        passwordResetService.sendResetOtp(request.getEmail())
                )
        );
    }

    // ===================== VERIFY RESET OTP =====================
    @Operation(summary = "Verify reset OTP")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OTP verified successfully",
                    content = @Content(schema = @Schema(implementation = ApiMessageResponse.class))
            )
    })
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiMessageResponse> verifyResetOtp(
            @RequestBody VerifyResetOtpRequest request
    ) {
        return ResponseEntity.ok(
                new ApiMessageResponse(
                        passwordResetService.verifyResetOtp(
                                request.getEmail(),
                                request.getOtp()
                        )
                )
        );
    }

    // ===================== RESET PASSWORD =====================
    @Operation(summary = "Reset password")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset successful",
                    content = @Content(schema = @Schema(implementation = ApiMessageResponse.class))
            )
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiMessageResponse> resetPassword(
            @RequestBody ResetPasswordRequest request
    ) {
        return ResponseEntity.ok(
                new ApiMessageResponse(
                        passwordResetService.resetPassword(
                                request.getEmail(),
                                request.getNewPassword(),
                                request.getConfirmNewPassword()
                        )
                )
        );
    }
}
