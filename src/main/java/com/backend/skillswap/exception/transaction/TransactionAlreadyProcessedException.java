package com.backend.skillswap.exception.transaction;

// prevent double release/refund
public class TransactionAlreadyProcessedException extends RuntimeException {
    public TransactionAlreadyProcessedException(String message) {
        super(message);
    }
}
