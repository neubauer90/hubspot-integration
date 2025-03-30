package com.meetime.hubspot.config;

import com.meetime.hubspot.adapter.gateway.HubSpotApiGateway;
import com.meetime.hubspot.adapter.gateway.TokenStorageGateway;
import com.meetime.hubspot.application.port.out.ExternalApiPort;
import com.meetime.hubspot.application.port.out.TokenStoragePort;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HubSpotConfig {

    @Value("${hubspot.client-id}")
    private String clientId;

    @Value("${hubspot.client-secret}")
    private String clientSecret;

    @Value("${hubspot.redirect-uri}")
    private String redirectUri;

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

    @Bean
    public TokenStoragePort tokenStoragePort() {
        return new TokenStorageGateway();
    }

    @Bean
    public ExternalApiPort externalApiPort(OkHttpClient client, TokenStoragePort tokenStoragePort) {
        return new HubSpotApiGateway(client, clientId, clientSecret, redirectUri, tokenStoragePort);
    }
}
