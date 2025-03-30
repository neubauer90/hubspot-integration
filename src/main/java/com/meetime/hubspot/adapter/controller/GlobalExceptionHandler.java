package com.meetime.hubspot.adapter.controller;

import com.meetime.hubspot.domain.exception.DailyRateLimitExceededException;
import com.meetime.hubspot.domain.exception.HubSpotException;
import com.meetime.hubspot.domain.exception.SecondlyRateLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HubSpotException.class)
    public ResponseEntity<String> handleHubSpotException(HubSpotException e) {
        String errorType = e.getErrorType();
        switch (errorType) {
            case "rate_limit_secondly":
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", "1")
                        .body(e.getMessage());
            case "rate_limit_daily":
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", "86400")
                        .body(e.getMessage());
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(e.getMessage());
        }
    }
    @ExceptionHandler(SecondlyRateLimitExceededException.class)
    public ResponseEntity<String> handleSecondlyRateLimitExceeded(SecondlyRateLimitExceededException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "1")
                .body(e.getMessage());
    }

    @ExceptionHandler(DailyRateLimitExceededException.class)
    public ResponseEntity<String> handleDailyRateLimitExceeded(DailyRateLimitExceededException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "86400")
                .body(e.getMessage());
    }
}
