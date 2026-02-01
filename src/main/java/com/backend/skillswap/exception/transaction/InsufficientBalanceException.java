package com.backend.skillswap.exception.transaction;

// withdrawal or escrow creation
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
