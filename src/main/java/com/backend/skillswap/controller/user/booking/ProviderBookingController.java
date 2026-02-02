package com.backend.skillswap.controller.user.booking;

import com.backend.skillswap.dto.response.BookingResponse;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.enums.BookingStatus;
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
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings/provider")
@RequiredArgsConstructor
@Tag(
        name = "12. Provider Booking APIs",
        description = """
Booking APIs for PROVIDERS (skill owner).

PROVIDER can:
• Confirm booking requests
• Start and complete sessions
• View upcoming & past bookings
• View bookings in date range
• Check availability for a time slot
"""
)
public class ProviderBookingController {

    private final BookingService bookingService;
    private final AuthService authService;

    // ================= CONFIRM BOOKING ===============================
    @Operation(
            summary = "Confirm booking",
            description = """
Allows PROVIDER to confirm a PENDING booking request.

Effects:
• Booking status changes from PENDING → CONFIRMED
• Escrow/payment transaction is created
"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking confirmed successfully",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Only pending bookings can be confirmed"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "409", description = "Booking slot conflict",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-01-13T17:03:16.389780400Z",
                                      "status": 409,
                                      "errorCode": "BOOKING_SLOT_CONFLICT",
                                      "message": "Booking slot overlaps"
                                    }
                                    """)))
    })
    @PreAuthorize("hasRole('PROVIDER')")
    @PatchMapping("/{bookingId}/confirm")
    public BookingResponse confirmBooking(@PathVariable Long bookingId) {
        UserEntity provider = authService.getCurrentUser();
        return bookingService.confirmBooking(bookingId, provider);
    }

    // ======================== START BOOKING ==================
    @Operation(
            summary = "Start booking session",
            description = """
Allows PROVIDER to start a CONFIRMED booking.

Effects:
• Booking status changes from CONFIRMED → IN_PROGRESS
"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking started successfully"),
            @ApiResponse(responseCode = "400", description = "Only confirmed bookings can be started"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PreAuthorize("hasRole('PROVIDER')")
    @PatchMapping("/{bookingId}/start")
    public BookingResponse startBooking(@PathVariable Long bookingId) {
        UserEntity provider = authService.getCurrentUser();
        return bookingService.startBooking(bookingId, provider);
    }

    // =========================== COMPLETE BOOKING =============
    @Operation(
            summary = "Complete booking session",
            description = """
Allows PROVIDER to mark an IN_PROGRESS booking as completed.

Effects:
• Booking status changes to COMPLETED
• Escrow payment is released to provider
"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking completed successfully"),
            @ApiResponse(responseCode = "400", description = "Only in-progress bookings can be completed"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PreAuthorize("hasRole('PROVIDER')")
    @PatchMapping("/{bookingId}/complete")
    public BookingResponse completeBooking(@PathVariable Long bookingId) {
        UserEntity provider = authService.getCurrentUser();
        return bookingService.completeBooking(bookingId, provider);
    }

    // ========================= UPCOMING BOOKINGS ===================
    @Operation(
            summary = "Get upcoming bookings",
            description = """
Fetch all upcoming bookings for the logged-in PROVIDER.

Includes:
• CONFIRMED
• IN_PROGRESS bookings
"""
    )
    @ApiResponse(responseCode = "200", description = "Upcoming bookings fetched successfully")
    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/upcoming")
    public List<BookingResponse> upcomingBookings() {
        UserEntity provider = authService.getCurrentUser();
        return bookingService.getUpcomingBookingsForProvider(provider).stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    // ======================== PAST BOOKINGS =====================
    @Operation(
            summary = "Get past bookings",
            description = """
Fetch all completed or cancelled bookings for the logged-in PROVIDER.
"""
    )
    @ApiResponse(responseCode = "200", description = "Past bookings fetched successfully")
    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/past")
    public List<BookingResponse> pastBookings() {
        UserEntity provider = authService.getCurrentUser();
        return bookingService.getPastBookingsForProvider(provider).stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    // =================== BOOKINGS IN DATE RANGE ==================
    @Operation(
            summary = "Get bookings in date range",
            description = """
Fetch provider bookings filtered by:
• Booking status
• Start and end date-time range

Useful for:
• Calendar view
• Analytics
"""
    )
    @ApiResponse(responseCode = "200", description = "Bookings fetched successfully")
    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/range")
    public List<BookingResponse> bookingsInRange(
            @RequestParam BookingStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        UserEntity provider = authService.getCurrentUser();
        return bookingService.getProviderBookingsInRange(provider, status, start, end).stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    // ================= AVAILABILITY CHECK FOR LOGGED-IN PROVIDER =============
    @Operation(
            summary = "Check logged-in provider availability",
            description = """
Checks whether the logged-in PROVIDER is available for a given time slot.

Use case:
• Before starting or confirming a booking
• Showing availability on UI
"""
    )
    @ApiResponse(responseCode = "200", description = "Availability checked successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/availability")
    public boolean checkProviderAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        UserEntity provider = authService.getCurrentUser();  // logged-in provider
        return bookingService.isSlotAvailable(provider, start, end);
    }
}
