package com.meetime.hubspot.adapter.gateway;

import com.meetime.hubspot.application.port.out.TokenStoragePort;

public class TokenStorageGateway implements TokenStoragePort {

    private String token;

    @Override
    public void saveToken(String token) {
        this.token = token;
    }

    @Override
    public String getToken() {
        return token;
    }
}
