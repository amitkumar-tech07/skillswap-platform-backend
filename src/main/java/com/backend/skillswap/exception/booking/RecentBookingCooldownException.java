package com.backend.skillswap.exception.booking;

// cooldown check failure
public class RecentBookingCooldownException extends RuntimeException {
    public RecentBookingCooldownException(String message) {
        super(message);
    }
}
