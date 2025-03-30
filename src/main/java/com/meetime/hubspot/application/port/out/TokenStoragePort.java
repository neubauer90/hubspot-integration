package com.meetime.hubspot.application.port.out;

public interface TokenStoragePort {
    void saveToken(String token);
    String getToken();
}
