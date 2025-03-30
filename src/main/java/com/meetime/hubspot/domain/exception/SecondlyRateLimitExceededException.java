package com.meetime.hubspot.domain.exception;

public class SecondlyRateLimitExceededException extends RateLimitExceededException {
    public SecondlyRateLimitExceededException(String message) {
        super(message);
    }
}
