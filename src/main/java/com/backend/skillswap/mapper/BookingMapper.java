package com.backend.skillswap.mapper;

import com.backend.skillswap.dto.request.BookingRequest;
import com.backend.skillswap.dto.response.BookingResponse;
import com.backend.skillswap.entity.Booking;

public class BookingMapper {

    private BookingMapper() {}

    // Entity -> Response DTO
    public static BookingResponse toResponse(Booking booking) {

        return BookingResponse.builder()
                .bookingId(booking.getId())

                // Skill info
                .skillId(booking.getSkill().getId())
                .skillName(booking.getSkill().getTitle())

                // User info
                .requesterId(booking.getRequester().getId())
                .providerId(booking.getProvider().getId())
                .providerName(
                        booking.getProvider().getUserProfile() != null
                                ? booking.getProvider().getUserProfile().getFullName()
                                : booking.getProvider().getEmail()
                )

                // Session info
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .durationMinutes(booking.getDurationMinutes())

                // Pricing snapshot
                .pricePerHour(booking.getPricePerHour())
                .totalAmount(booking.getTotalAmount())

                // Status
                .status(booking.getStatus())
                .cancelReason(booking.getCancelReason())

                // Optional: default message (UI clarity)
                .message(null)

                // Audit
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())

                .build();
    }

    // Request DTO -> Entity
    public static Booking toEntity(BookingRequest request) {
        return Booking.builder()
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationMinutes(request.getDurationMinutes())
                .build();
    }
}
