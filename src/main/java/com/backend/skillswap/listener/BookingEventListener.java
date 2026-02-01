package com.backend.skillswap.listener;

import com.backend.skillswap.events.BookingEvent;
import com.backend.skillswap.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventListener {

    private final EmailService emailService;

    // Sends emails to both requester and provider based on event type & It Centralized, error-safe, non-blocking notification handling
    @Async
    @EventListener
    public void handleBookingEvent(BookingEvent event) {
        try {
            switch (event.getEventType()) {

                case "BOOKING_CREATED":
                    emailService.sendBookingCreatedMail(event.getBooking());           // Requester notification
                    emailService.sendProviderBookingCreatedMail(event.getBooking());   // Provider notification
                    break;

                case "BOOKING_CONFIRMED":
                    emailService.sendBookingConfirmedMail(event.getBooking());         // Requester confirmation
                    emailService.sendProviderBookingConfirmedMail(event.getBooking()); // Provider confirmation
                    break;

                case "BOOKING_CANCELLED":
                    emailService.sendBookingCancelledMail(event.getBooking());         // Requester cancellation
                    emailService.sendProviderBookingCancelledMail(event.getBooking()); // Provider cancellation
                    break;

                case "BOOKING_STARTED":
                    emailService.sendBookingStartedMail(event.getBooking());           // Notify session started
                    break;

                case "BOOKING_COMPLETED":
                    emailService.sendBookingCompletedMail(event.getBooking());         // Notify session completed
                    break;

                case "BOOKING_DISPUTED":
                    emailService.sendBookingDisputedMail(event.getBooking());          // Notify dispute raised
                    break;
            }
        } catch (Exception e) { // ❌ Error aaye to app crash nahi karegi
            log.error("Failed to process booking event: {}", event.getEventType(), e);
        }
    }
}

