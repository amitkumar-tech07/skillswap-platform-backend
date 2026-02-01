package com.backend.skillswap.controller.user.booking;

import com.backend.skillswap.dto.request.BookingRequest;
import com.backend.skillswap.dto.response.BookingResponse;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.exception.common.ResourceNotFoundException;
import com.backend.skillswap.mapper.BookingMapper;
import com.backend.skillswap.service.AuthService;
import com.backend.skillswap.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings/user")
@RequiredArgsConstructor
@Tag(
        name = "User Booking APIs",
        description = """
Booking APIs for USERS (requester).

USER can:
• Create booking after skill request is accepted
• View upcoming and past bookings
• Check own availability before booking
"""
)
public class UserBookingController {

    private final BookingService bookingService;
    private final AuthService authService;

    // ======================== CREATE BOOKING =====================
    @Operation(
            summary = "Create a new booking",
            description = """
Allows USER (requester) to create a booking for an ACCEPTED skill request.

Rules:
• Only requester can create booking
• Booking time must be valid
• Slot must not overlap with existing bookings
• Booking status will be PENDING initially
"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created successfully",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid booking data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "409", description = "Booking slot conflict",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-01-13T17:03:16.389780400Z",
                                      "status": 409,
                                      "errorCode": "BOOKING_SLOT_CONFLICT",
                                      "message": "Provider is not available for this slot"
                                    }
                                    """)))
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public BookingResponse createBooking(@Valid @RequestBody BookingRequest request) {
        UserEntity user = authService.getCurrentUser();
        return bookingService.createBooking(user, request);
    }

    // =================== UPCOMING BOOKINGS ============================
    @Operation(
            summary = "Get upcoming bookings",
            description = """
Fetch all upcoming bookings for the logged-in USER.

Includes:
• PENDING
• CONFIRMED
• IN_PROGRESS bookings
"""
    )
    @ApiResponse(responseCode = "200", description = "Upcoming bookings fetched successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/upcoming")
    public List<BookingResponse> upcomingBookings() {
        UserEntity user = authService.getCurrentUser();
        return bookingService.getUpcomingBookingsForRequester(user).stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    // ====================== PAST BOOKINGS =================
    @Operation(
            summary = "Get past bookings",
            description = """
Fetch all completed or cancelled bookings for the logged-in USER.

Includes:
• COMPLETED
• CANCELLED bookings
"""
    )
    @ApiResponse(responseCode = "200", description = "Past bookings fetched successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/past")
    public List<BookingResponse> pastBookings() {
        UserEntity user = authService.getCurrentUser();
        return bookingService.getPastBookingsForRequester(user).stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    // ==================== AVAILABILITY CHECK =================
    @Operation(
            summary = "Check user availability",
            description = """
Checks whether the USER is available for a given time slot.

Use case:
• Before creating a booking
• Prevent overlapping bookings
"""
    )
    @ApiResponse(responseCode = "200", description = "Availability checked successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/availability")
    public boolean checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        UserEntity user = authService.getCurrentUser();
        return bookingService.isRequesterAvailable(user, start, end);
    }


    // Auth-safe Booking Fetch
    @Operation(
            summary = "Fetch booking securely (Requester only)",
            description = """
Allows USER (requester) to fetch booking details securely.

Rules:
• Only requester of the booking can access
• Provider cannot access via this endpoint
"""
    )
    @ApiResponse(responseCode = "200", description = "Booking fetched successfully")
    @ApiResponse(responseCode = "403", description = "Not allowed to access this booking")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{bookingId}/secure")
    public BookingResponse authSafeFetch(@PathVariable Long bookingId) {
        UserEntity user = authService.getCurrentUser();
        return BookingMapper.toResponse(
                bookingService.getBookingByIdForRequester(bookingId, user)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found"))
        );
    }
}
