package com.meetime.hubspot.domain.exception;

public class HubSpotException extends RuntimeException {
    private final String errorType;

    public HubSpotException(String message, String errorType) {
        super(message);
        this.errorType = errorType;
    }

    public HubSpotException(String message) {
        this(message, "unknown");
    }

    public String getErrorType() {
        return errorType;
    }
}
