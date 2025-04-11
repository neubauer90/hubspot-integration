package com.meetime.hubspot.application.port.in;

public interface OAuthUseCase {
    String getAuthorizationUrl(String state);
    void handleCallback(String code);
}