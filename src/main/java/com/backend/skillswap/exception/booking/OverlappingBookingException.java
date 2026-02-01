package com.backend.skillswap.exception.booking;

// provider/user slot conflict
public class OverlappingBookingException extends RuntimeException {
    public OverlappingBookingException(String message) {
        super(message);
    }
}
