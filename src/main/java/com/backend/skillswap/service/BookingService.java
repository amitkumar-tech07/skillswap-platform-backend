package com.backend.skillswap.service;

import com.backend.skillswap.dto.request.BookingRequest;
import com.backend.skillswap.dto.response.BookingResponse;
import com.backend.skillswap.entity.Booking;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingService {

    // CREATE
    BookingResponse createBooking(UserEntity requester, BookingRequest request);

    // CONFIRM
    BookingResponse confirmBooking(Long bookingId, UserEntity provider);

    // CANCEL
    BookingResponse cancelBooking(Long bookingId, UserEntity user, String reason);

    // START / COMPLETE
    BookingResponse startBooking(Long bookingId, UserEntity provider);
    BookingResponse completeBooking(Long bookingId, UserEntity provider);

    // FETCH
    Optional<Booking> getBookingById(Long bookingId);
    List<Booking> getBookingsByRequester(UserEntity requester);
    List<Booking> getBookingsByProvider(UserEntity provider);

    // UPCOMING / PAST BOOKINGS
    List<Booking> getUpcomingBookingsForProvider(UserEntity provider);
    List<Booking> getUpcomingBookingsForRequester(UserEntity requester);

    List<Booking> getPastBookingsForProvider(UserEntity provider);
    List<Booking> getPastBookingsForRequester(UserEntity requester);

    // VALIDATIONS
    boolean isSlotAvailable(UserEntity provider, LocalDateTime start, LocalDateTime end);
    boolean isRequesterAvailable(UserEntity requester, LocalDateTime start, LocalDateTime end);


    // FILTER BY STATUS
    List<Booking> getBookingsByProviderAndStatus(UserEntity provider, BookingStatus status);
    List<Booking> getBookingsByRequesterAndStatus(UserEntity requester, BookingStatus status);

    // FILTER BY SKILL
    List<Booking> getBookingsBySkill(Long skillId);
    List<Booking> getBookingsBySkillAndStatus(Long skillId, BookingStatus status);

    // AUTH-SAFE FETCH
    Optional<Booking> getBookingByIdForRequester(Long bookingId, UserEntity requester);

    // Dispute
    public BookingResponse raiseDispute(
            Long bookingId,
            UserEntity user,
            String reason
    );

    List<Booking> getBookingsByStatus(BookingStatus status);

    List<Booking> getProviderBookingsInRange(
            UserEntity provider,
            BookingStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

}
