package com.backend.skillswap.exception.transaction;

// no escrow exists for a booking
public class EscrowNotFoundException extends RuntimeException {
    public EscrowNotFoundException(String message) {
        super(message);
    }
}
