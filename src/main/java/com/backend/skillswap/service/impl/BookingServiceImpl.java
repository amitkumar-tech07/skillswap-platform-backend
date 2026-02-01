package com.backend.skillswap.service.impl;

import com.backend.skillswap.dto.request.BookingRequest;
import com.backend.skillswap.dto.response.BookingResponse;
import com.backend.skillswap.entity.Booking;
import com.backend.skillswap.entity.SkillRequest;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.UserSkill;
import com.backend.skillswap.entity.enums.BookingStatus;
import com.backend.skillswap.entity.enums.CancelBooking;
import com.backend.skillswap.entity.enums.SkillRequestStatus;
import com.backend.skillswap.events.BookingEvent;
import com.backend.skillswap.events.BookingEventType;
import com.backend.skillswap.exception.booking.OverlappingBookingException;
import com.backend.skillswap.exception.booking.RecentBookingCooldownException;
import com.backend.skillswap.exception.common.BadRequestException;
import com.backend.skillswap.exception.common.OperationNotAllowedException;
import com.backend.skillswap.exception.common.ResourceNotFoundException;
import com.backend.skillswap.mapper.BookingMapper;
import com.backend.skillswap.repository.BookingRepository;
import com.backend.skillswap.repository.SkillRequestRepository;
import com.backend.skillswap.repository.UserSkillRepository;
import com.backend.skillswap.service.BookingService;
import com.backend.skillswap.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserSkillRepository skillRepository;
    private final TransactionService transactionService;
    private final SkillRequestRepository skillRequestRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Lifecycle enforcement (PENDING → CONFIRMED → IN_PROGRESS → COMPLETED / CANCELLED / DISPUTED)  (Authorization checks in Every Step )
    // ================= CREATE BOOKING =================
    @Override
    @Transactional
    public BookingResponse createBooking(UserEntity requester, BookingRequest request) {

        // Validate start/end time & duration
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new BadRequestException("Start time and End time are required");
        }

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        long minutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (minutes <= 0) {
            throw new BadRequestException("Booking duration must be greater than zero");
        }

        // Fetch SkillRequest
        SkillRequest skillRequest =
                skillRequestRepository.findByIdForUpdate(request.getSkillRequestId())
                        .orElseThrow(() -> new ResourceNotFoundException("Skill request not found"));

        if (skillRequest.getStatus() != SkillRequestStatus.ACCEPTED) {
            throw new BadRequestException("Only ACCEPTED skill request can be booked");
        }

        // Authorization: only sender can book
        if (!skillRequest.getSender().getId().equals(requester.getId())) {
            throw new OperationNotAllowedException("Only request sender can create booking");
        }

        UserEntity provider = skillRequest.getReceiver();
        UserSkill skill = skillRequest.getSkill();

        // Check slot availability
        boolean providerOverlap = bookingRepository.existsOverlappingBookingForProvider(
                provider.getId(), request.getStartTime(), request.getEndTime());
        if (providerOverlap) {
            throw new OverlappingBookingException("Provider is not available for this slot");
        }

        boolean userOverlap = bookingRepository.existsOverlappingBookingForUser(
                requester.getId(), request.getStartTime(), request.getEndTime());
        if (userOverlap) {
            throw new OverlappingBookingException("You already have another booking in this slot");
        }

        // Cooldown check
        boolean hasRecent = bookingRepository.hasRecentBooking(
                requester.getId(), provider.getId(), LocalDateTime.now().minusMinutes(1));
        if (hasRecent) {
            throw new RecentBookingCooldownException("Please wait 1 min before booking again");
        }

        // Create Booking entity (pure mapping)
        Booking booking = BookingMapper.toEntity(request);

        booking.setRequester(requester);
        booking.setProvider(provider);
        booking.setSkill(skill);
        booking.setRequest(skillRequest);

        // Business logic: duration, pricing, totalAmount, status
        int durationMinutes = request.getDurationMinutes() != null ? request.getDurationMinutes() : (int) minutes;
        booking.setDurationMinutes(durationMinutes);
        booking.setPricePerHour(skill.getHourlyRate());

        BigDecimal hours = BigDecimal.valueOf(durationMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = skill.getHourlyRate().multiply(hours);
        booking.setTotalAmount(totalAmount);

        booking.setStatus(BookingStatus.PENDING);

        // Save booking
        Booking savedBooking = bookingRepository.save(booking);

        // Lock SkillRequest
        skillRequest.setStatus(SkillRequestStatus.BOOKED);
        skillRequestRepository.save(skillRequest);

        // Publish event
        eventPublisher.publishEvent(new BookingEvent(savedBooking, BookingEventType.CREATED.name()));

        // Return response
        return BookingMapper.toResponse(savedBooking);
    }

    // ================= CONFIRM BOOKING =================
    @Transactional
    @Override
    public BookingResponse confirmBooking(Long bookingId, UserEntity provider) {

        Booking booking = bookingRepository.findByIdAndProvider(bookingId, provider)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new OperationNotAllowedException("Only pending bookings can be confirmed");
        }

        // Confirm booking FIRST
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking updated = bookingRepository.save(booking);

        // Escrow created ONLY ON CONFIRM , Note: Balance insufficient --> booking never CONFIRMED
        transactionService.createEscrowTransaction(  // balance check happens here
                updated.getRequester(),  // payer
                updated,    // booking
                updated.getTotalAmount(),  // amount
                null   // type (null defaults to ESCROW)
        );

        eventPublisher.publishEvent(new BookingEvent(updated, BookingEventType.CONFIRMED.name()));

        return BookingMapper.toResponse(updated);
    }

    @Transactional
    @Override
    public BookingResponse cancelBooking(Long bookingId, UserEntity user, String reason) {

        // Fetch booking
        Booking booking = bookingRepository.findByIdWithRequest(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Already cancelled → block
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new OperationNotAllowedException("Booking already cancelled");
        }

        // Completed booking cannot be cancelled
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new OperationNotAllowedException("Completed booking cannot be cancelled");
        }

        // Authorization
        if (!booking.getRequester().equals(user)
                && !booking.getProvider().equals(user)) {
            throw new OperationNotAllowedException("Not allowed to cancel this booking");
        }

        // Reason validation
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("Cancel reason is required");
        }

        BookingStatus oldStatus = booking.getStatus();

        // Mark booking CANCELLED
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledBy(
                booking.getRequester().equals(user)
                        ? CancelBooking.USER
                        : CancelBooking.PROVIDER
        );
        booking.setCancelReason(reason);

        Booking updatedBooking = bookingRepository.save(booking);

        // Refund ONLY if escrow was created
        if (oldStatus == BookingStatus.CONFIRMED || oldStatus == BookingStatus.IN_PROGRESS) {
            transactionService.refund(updatedBooking.getId());
        }

        // Unlock SkillRequest (if locked)
        SkillRequest skillRequest = updatedBooking.getRequest();
        if (skillRequest != null && skillRequest.getStatus() == SkillRequestStatus.BOOKED) {

            skillRequest.setStatus(SkillRequestStatus.ACCEPTED);
            skillRequestRepository.save(skillRequest);
        }

        // Publish event
        eventPublisher.publishEvent(new BookingEvent(updatedBooking, BookingEventType.CANCELLED.name()));

        return BookingMapper.toResponse(updatedBooking);
    }

    // ================= START BOOKING =================
    @Transactional
    @Override
    public BookingResponse startBooking(Long bookingId, UserEntity provider) {

        Booking booking = bookingRepository.findByIdAndProvider(bookingId, provider)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new OperationNotAllowedException("Booking must be confirmed before starting");
        }

        booking.setStatus(BookingStatus.IN_PROGRESS);
        Booking updated = bookingRepository.save(booking);

        eventPublisher.publishEvent(new BookingEvent(booking, BookingEventType.STARTED.name()));

        return BookingMapper.toResponse(updated);
    }

    @Transactional
    @Override
    public BookingResponse completeBooking(Long bookingId, UserEntity provider) {

        // Fetch booking with provider + request
        Booking booking = bookingRepository
                .findByIdWithRequestAndProvider(bookingId, provider)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Only IN_PROGRESS booking can be completed
        if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new OperationNotAllowedException("Booking must be in progress to complete");
        }

        // Mark booking COMPLETED (same transaction)
        booking.setStatus(BookingStatus.COMPLETED);
        Booking updatedBooking = bookingRepository.save(booking);

        // SAME TRANSACTION — status visible
        transactionService.releaseEscrow(booking.getId());

        // Update SkillRequest if exists
        SkillRequest skillRequest = updatedBooking.getRequest();
        if (skillRequest != null) {
            skillRequest.setStatus(SkillRequestStatus.COMPLETED);
            skillRequestRepository.save(skillRequest);
        }

        // Publish event
        eventPublisher.publishEvent(new BookingEvent(updatedBooking, BookingEventType.COMPLETED.name()));

        return BookingMapper.toResponse(updatedBooking);
    }

    // ================= FETCH =================
    @Override
    public Optional<Booking> getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }

    @Override
    public List<Booking> getBookingsByRequester(UserEntity requester) {
        return bookingRepository.findByRequester(requester);
    }

    @Override
    public List<Booking> getBookingsByProvider(UserEntity provider) {
        return bookingRepository.findByProvider(provider);
    }

    // ================= UPCOMING =================
    @Override
    public List<Booking> getUpcomingBookingsForProvider(UserEntity provider) {
        return bookingRepository.findUpcomingBookingsForProvider(
                provider,
                LocalDateTime.now()
        );
    }

    @Override
    public List<Booking> getUpcomingBookingsForRequester(UserEntity requester) {
        return bookingRepository.findUpcomingBookingsForRequester(
                requester, LocalDateTime.now()
        );
    }

    // ================= PAST =================
    @Override
    public List<Booking> getPastBookingsForProvider(UserEntity provider) {
        return bookingRepository.findPastBookingsForProvider(provider);
    }

    @Override
    public List<Booking> getPastBookingsForRequester(UserEntity requester) {
        return bookingRepository.findPastBookingsForRequester(requester);
    }

    // ================= AVAILABILITY =================
    @Override
    public boolean isSlotAvailable(UserEntity provider, LocalDateTime start, LocalDateTime end) {
        return !bookingRepository.existsOverlappingBookingForProvider(
                provider.getId(), start, end
        );
    }

    @Override
    public boolean isRequesterAvailable(UserEntity requester, LocalDateTime start, LocalDateTime end) {
        return !bookingRepository.existsOverlappingBookingForUser(
                requester.getId(), start, end
        );
    }

    @Override
    public List<Booking> getBookingsByProviderAndStatus(UserEntity provider, BookingStatus status) {
        return bookingRepository.findByProviderAndStatus(provider, status);
    }

    @Override
    public List<Booking> getBookingsByRequesterAndStatus(UserEntity requester, BookingStatus status) {
        return bookingRepository.findByRequesterAndStatus(requester, status);
    }

    @Override
    public List<Booking> getBookingsBySkill(Long skillId) {
        UserSkill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
        return bookingRepository.findBySkill(skill);
    }

    @Override
    public List<Booking> getBookingsBySkillAndStatus(Long skillId, BookingStatus status) {
        UserSkill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
        return bookingRepository.findBySkillAndStatus(skill, status);
    }

    @Override
    public Optional<Booking> getBookingByIdForRequester(Long bookingId, UserEntity requester) {
        return bookingRepository.findByIdAndRequester(bookingId, requester);
    }

    // Dispute allowed only after COMPLETED.
    @Override
    public BookingResponse raiseDispute(Long bookingId, UserEntity user, String reason) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() == BookingStatus.DISPUTED) {
            throw new OperationNotAllowedException("Booking already disputed");
        }

        if (!booking.getRequester().equals(user)
                && !booking.getProvider().equals(user)) {
            throw new OperationNotAllowedException("Not allowed");
        }

        // Dispute allowed only after COMPLETED.
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new OperationNotAllowedException("Only completed bookings can be disputed");
        }

        booking.setStatus(BookingStatus.DISPUTED);
        booking.setDisputeReason(reason);

        Booking updated = bookingRepository.save(booking);

        eventPublisher.publishEvent(
                new BookingEvent(updated, BookingEventType.DISPUTED.name())
        );

        return BookingMapper.toResponse(updated);
    }

    @Override
    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }

    @Override
    public List<Booking> getProviderBookingsInRange(UserEntity provider, BookingStatus status, LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Start time must be before end time");
        }

        return bookingRepository.findProviderBookingsWithinDateRange(provider, status, start, end);
    }
}
