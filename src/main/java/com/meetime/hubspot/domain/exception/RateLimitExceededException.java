package com.meetime.hubspot.domain.exception;

public abstract class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
