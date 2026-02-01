package com.backend.skillswap.exception.authentication;

public class OtpBlockedException extends RuntimeException {
    public OtpBlockedException(String message) {
        super(message);
    }
}
