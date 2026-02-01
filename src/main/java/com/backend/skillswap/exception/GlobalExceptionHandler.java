package com.backend.skillswap.exception;

import com.backend.skillswap.dto.common.ApiErrorResponse;
import com.backend.skillswap.exception.authentication.*;
import com.backend.skillswap.exception.booking.OverlappingBookingException;
import com.backend.skillswap.exception.booking.RateLimitExceededException;
import com.backend.skillswap.exception.booking.RecentBookingCooldownException;
import com.backend.skillswap.exception.common.*;
import com.backend.skillswap.exception.transaction.EscrowNotFoundException;
import com.backend.skillswap.exception.transaction.InsufficientBalanceException;
import com.backend.skillswap.exception.transaction.TransactionAlreadyProcessedException;
import com.backend.skillswap.exception.transaction.TransactionFailedException;
import com.backend.skillswap.exception.userSkill.SkillDeletionNotAllowedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;


/**
 * =====================================================
 * 🌍 GLOBAL EXCEPTION HANDLER
 * =====================================================
 * Centralized, consistent & secure exception handling
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    // ================= DUPLICATE EMAIL =================
    // Email or username already exists → 409
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(
            DuplicateEmailException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateUsername(
            DuplicateUsernameException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "DUPLICATE_USERNAME",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateUser(
            DuplicateUserException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "DUPLICATE_USER",
                ex.getMessage(),
                request
        );
    }

    // ================= INVALID CREDENTIALS =================
    // Wrong password / invalid login → 401
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage(), request);
    }

    // ================= EMAIL NOT VERIFIED =================
    // Login attempted before email verification → 403
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailNotVerified(
            EmailNotVerifiedException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED", ex.getMessage(), request);
    }

    // ================= OTP EXPIRED =================
    // OTP expired → 410 Gone (resource no longer valid)
    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handleOtpExpired(
            OtpExpiredException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.GONE, "OTP_EXPIRED", ex.getMessage(), request);
    }

    // ================= OTP BLOCKED =================
    // Too many wrong OTP attempts → 429 Too Many Requests
    @ExceptionHandler(OtpBlockedException.class)
    public ResponseEntity<ApiErrorResponse> handleOtpBlocked(
            OtpBlockedException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.TOO_MANY_REQUESTS, "OTP_BLOCKED", ex.getMessage(), request);
    }

    // ================= TOKEN EXPIRED =================
    // JWT / refresh token expired → 401
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handleTokenExpired(
            TokenExpiredException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", ex.getMessage(), request);
    }

    // ================= VALIDATION ERROR =================
    // DTO validation failure → 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err -> err.getField() + " : " + err.getDefaultMessage())
                .orElse("Validation failed");

        return ResponseEntity.badRequest().body(
                ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(400)
                        .errorCode("VALIDATION_ERROR")
                        .message(message)
                        .path(request.getRequestURI())
                        .build()
        );
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleEnumParseError(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        String message = "Invalid value provided for category or level";

        // Optional: more user-friendly message
        if (ex.getMessage() != null && ex.getMessage().contains("SkillCategory")) {
            message = "Invalid skill category. Allowed values: PROGRAMMING, MUSIC, LANGUAGE, OTHER";
        }
        else if (ex.getMessage() != null && ex.getMessage().contains("SkillLevel")) {
            message = "Invalid skill level. Allowed values: BEGINNER, INTERMEDIATE, EXPERT";
        }

        return ResponseEntity.badRequest().body(
                ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(400)
                        .errorCode("INVALID_ENUM_VALUE")
                        .message(message)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "INVALID_STATE",
                ex.getMessage(),
                request
        );
    }


    // ================= INVALID ENUM / ARG =================
    // Wrong enum / invalid value → 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_REQUEST, "INVALID_VALUE", ex.getMessage(), request);
    }

    // ================= RESOURCE NOT FOUND =================
    // Entity not found → 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    // ================= BUSINESS RULE ERROR =================
    // Business validation failure → 400
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request);
    }

    // ================= ACCESS DENIED =================
    // Role / authority failure → 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You do not have permission to access this resource",
                request
        );
    }

    // ================= EMAIL SEND FAILURE =================
    // Mail service failure → 500
    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailSend(
            EmailSendException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SEND_FAILED", ex.getMessage(), request);
    }

    // ================= FALLBACK =================
    // Any unhandled exception → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Something went wrong. Please try again later.",
                request
        );
    }

    // ================= COMMON RESPONSE BUILDER =================
    // DRY method to build uniform error responses
    private ResponseEntity<ApiErrorResponse> buildError(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(status.value())
                        .errorCode(errorCode)
                        .message(message)
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRefreshToken(
            InvalidRefreshTokenException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.UNAUTHORIZED,
                "INVALID_REFRESH_TOKEN",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(OtpNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOtpNotFound(
            OtpNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "OTP_NOT_FOUND",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimitExceeded(
            RateLimitExceededException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.TOO_MANY_REQUESTS,
                "RATE_LIMIT_EXCEEDED",
                ex.getMessage(),
                request
        );
    }


    // ================= BOOKING: OVERLAPPING SLOT =================
    @ExceptionHandler(OverlappingBookingException.class)
    public ResponseEntity<ApiErrorResponse> handleOverlappingBooking(
            OverlappingBookingException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "BOOKING_SLOT_CONFLICT",
                ex.getMessage(),
                request
        );
    }

    // ================= BOOKING: COOLDOWN =================
    @ExceptionHandler(RecentBookingCooldownException.class)
    public ResponseEntity<ApiErrorResponse> handleBookingCooldown(
            RecentBookingCooldownException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.TOO_MANY_REQUESTS,
                "BOOKING_COOLDOWN_ACTIVE",
                ex.getMessage(),
                request
        );
    }

    // ================= BOOKING: INVALID OPERATION =================
    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleOperationNotAllowed(
            OperationNotAllowedException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "OPERATION_NOT_ALLOWED",
                ex.getMessage(),
                request
        );
    }

    // ================= TRANSACTION: INSUFFICIENT BALANCE =================
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientBalance(
            InsufficientBalanceException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "INSUFFICIENT_BALANCE",
                ex.getMessage(),
                request
        );
    }

    // ================= TRANSACTION: ESCROW NOT FOUND =================
    @ExceptionHandler(EscrowNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEscrowNotFound(
            EscrowNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "ESCROW_NOT_FOUND",
                ex.getMessage(),
                request
        );
    }

    // ================= TRANSACTION: ALREADY PROCESSED =================
    @ExceptionHandler(TransactionAlreadyProcessedException.class)
    public ResponseEntity<ApiErrorResponse> handleTransactionAlreadyProcessed(
            TransactionAlreadyProcessedException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "TRANSACTION_ALREADY_PROCESSED",
                ex.getMessage(),
                request
        );
    }

    // ================= TRANSACTION: FAILED =================
    @ExceptionHandler(TransactionFailedException.class)
    public ResponseEntity<ApiErrorResponse> handleTransactionFailed(
            TransactionFailedException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "TRANSACTION_FAILED",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "DUPLICATE_RESOURCE",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(SkillDeletionNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleSkillDeletionNotAllowed(
            SkillDeletionNotAllowedException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "SKILL_DELETION_NOT_ALLOWED",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(
            InvalidRequestException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                ex.getMessage(),
                request
        );
    }




}


/*🧠 INTERVIEW ONE-LINER (FINAL – USE THIS)

“I implemented centralized exception handling using @ControllerAdvice, mapped authentication,
authorization, OTP, token lifecycle, and business rule failures to correct HTTP status codes, and
returned consistent, secure error responses without leaking sensitive information.”*/