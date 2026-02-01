package com.backend.skillswap.controller.user.booking;

import com.backend.skillswap.dto.request.DisputeRequest;
import com.backend.skillswap.dto.response.BookingResponse;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.enums.BookingStatus;
import com.backend.skillswap.entity.enums.Role;
import com.backend.skillswap.mapper.BookingMapper;
import com.backend.skillswap.service.AuthService;
import com.backend.skillswap.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings/shared")
@RequiredArgsConstructor
@Tag(name = "Shared Booking APIs", description = "Endpoints usable by both User & Provider")
public class SharedBookingController {

    private final BookingService bookingService;
    private final AuthService authService;

    @Operation(
            summary = "Cancel a booking",
            description = """
Allows USER or PROVIDER to cancel an existing booking.

Rules:
• Only involved USER or PROVIDER can cancel
• Reason is mandatory
• Refund / penalty logic is handled internally
"""
    )
    @ApiResponse(responseCode = "200", description = "Booking cancelled successfully")
    @ApiResponse(responseCode = "403", description = "You are not allowed to cancel this booking")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    @PreAuthorize("hasAnyRole('USER','PROVIDER')")
    @PatchMapping("/{bookingId}/cancel")
    public BookingResponse cancelBooking(@PathVariable Long bookingId,
                                         @RequestParam String reason) {
        UserEntity user = authService.getCurrentUser();
        return bookingService.cancelBooking(bookingId, user, reason);
    }

    // Raise Dispute
    @Operation(
            summary = "Raise a dispute for a booking",
            description = """
Allows USER or PROVIDER to raise a dispute on a booking.

Use cases:
• Service quality issue
• Payment disagreement
• Behaviour issues

Dispute reason is mandatory.
"""
    )
    @ApiResponse(responseCode = "200", description = "Dispute raised successfully")
    @ApiResponse(responseCode = "400", description = "Invalid dispute request")
    @ApiResponse(responseCode = "403", description = "You are not part of this booking")
    @PreAuthorize("hasAnyRole('USER','PROVIDER')")
    @PutMapping("/{bookingId}/dispute")
    public BookingResponse raiseDispute(@PathVariable Long bookingId,
                                        @RequestBody DisputeRequest request) {
        UserEntity user = authService.getCurrentUser();
        return bookingService.raiseDispute(bookingId, user, request.getReason());
    }

    // Get Bookings by Status
    @Operation(
            summary = "Get my bookings by status",
            description = """
Fetch bookings filtered by status.

Behaviour:
• USER → gets bookings where user is requester
• PROVIDER → gets bookings where user is provider
"""
    )
    @ApiResponse(responseCode = "200", description = "Bookings fetched successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @PreAuthorize("hasAnyRole('USER','PROVIDER')")
    @GetMapping("/status/{status}")
    public List<BookingResponse> bookingsByStatus(@PathVariable BookingStatus status) {
        UserEntity user = authService.getCurrentUser();
        boolean isProvider = user.getRoles().stream().anyMatch(r -> r == Role.PROVIDER);
        return (isProvider
                ? bookingService.getBookingsByProviderAndStatus(user, status)
                : bookingService.getBookingsByRequesterAndStatus(user, status))
                .stream().map(BookingMapper::toResponse).toList();
    }

    // Get Bookings by Skill
    @Operation(
            summary = "Get bookings by skill",
            description = """
Fetch all bookings related to a specific skill.

Use case:
• Analytics
• Skill-based history view
"""
    )
    @ApiResponse(responseCode = "200", description = "Bookings fetched successfully")
    @ApiResponse(responseCode = "404", description = "Skill not found")
    @GetMapping("/skill/{skillId}")
    public List<BookingResponse> bookingsBySkill(@PathVariable Long skillId) {
        return bookingService.getBookingsBySkill(skillId).stream()
                .map(BookingMapper::toResponse).toList();
    }

    // Get Bookings by Skill + Status
    @Operation(
            summary = "Get bookings by skill and status",
            description = """
Fetch bookings filtered by both skill and booking status.

Example:
• All COMPLETED bookings for Java skill
"""
    )
    @ApiResponse(responseCode = "200", description = "Bookings fetched successfully")
    @GetMapping("/skill/{skillId}/status/{status}")
    public List<BookingResponse> bookingsBySkillAndStatus(@PathVariable Long skillId,
                                                          @PathVariable BookingStatus status) {
        return bookingService.getBookingsBySkillAndStatus(skillId, status).stream()
                .map(BookingMapper::toResponse).toList();
    }
}
