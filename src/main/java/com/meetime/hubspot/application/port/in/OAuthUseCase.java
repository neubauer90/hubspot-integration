package com.meetime.hubspot.application.port.in;

public interface OAuthUseCase {
    String getAuthorizationUrl();
    void handleCallback(String code);
}
