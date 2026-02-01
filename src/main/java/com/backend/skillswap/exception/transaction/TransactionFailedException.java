package com.backend.skillswap.exception.transaction;

// failed escrow release or refund
public class TransactionFailedException extends RuntimeException {
    public TransactionFailedException(String message) {
        super(message);
    }
}
