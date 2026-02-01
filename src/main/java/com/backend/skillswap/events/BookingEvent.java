package com.backend.skillswap.events;

import com.backend.skillswap.entity.Booking;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class BookingEvent {

    // The booking associated with this event
    private final Booking booking;

    // Type of event (e.g., BOOKING_CREATED, BOOKING_CONFIRMED, STARTED ,COMPLETED etc.)
    private final String eventType;

    public BookingEvent(Booking booking, String eventType) {
        this.booking = booking;
        this.eventType = eventType;
    }
}
