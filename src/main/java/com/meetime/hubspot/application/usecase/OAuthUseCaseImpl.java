package com.meetime.hubspot.application.usecase;

import com.meetime.hubspot.application.port.in.OAuthUseCase;
import com.meetime.hubspot.application.port.out.ExternalApiPort;
import com.meetime.hubspot.application.port.out.TokenStoragePort;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@Getter
public class OAuthUseCaseImpl implements OAuthUseCase {

    @Value("${hubspot.client-id}")
    private String clientId;

    @Value("${hubspot.redirect-uri}")
    private String redirectUri;

    @Value("${hubspot.scope}")
    private String scope;

    @Autowired
    private ExternalApiPort externalApiPort;

    @Autowired
    private TokenStoragePort tokenStoragePort;

    @Override
    public String getAuthorizationUrl() {
        return "https://app.hubspot.com/oauth/authorize" +
                "?client_id=" + clientId +
                "&scope=" + scope +
                "&redirect_uri=" + redirectUri;
    }

    @Override
    public void handleCallback(String code) {
        String token = externalApiPort.exchangeCodeForToken(code);
        tokenStoragePort.saveToken(token);
    }
}
