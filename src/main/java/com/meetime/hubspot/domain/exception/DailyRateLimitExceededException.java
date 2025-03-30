package com.meetime.hubspot.domain.exception;

public class DailyRateLimitExceededException extends RateLimitExceededException {
    public DailyRateLimitExceededException(String message) {
        super(message);
    }
}
