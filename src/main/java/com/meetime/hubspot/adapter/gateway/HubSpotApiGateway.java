package com.meetime.hubspot.adapter.gateway;

import com.meetime.hubspot.application.port.out.ExternalApiPort;
import com.meetime.hubspot.application.port.out.TokenStoragePort;
import com.meetime.hubspot.domain.entity.Contact;
import com.meetime.hubspot.util.HttpUtils;
import okhttp3.*;
import org.json.JSONObject;
import com.meetime.hubspot.domain.exception.HubSpotException;

public class HubSpotApiGateway implements ExternalApiPort {

    private final OkHttpClient client;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final TokenStoragePort tokenStoragePort;

    public HubSpotApiGateway(OkHttpClient client, String clientId, String clientSecret,
                             String redirectUri, TokenStoragePort tokenStoragePort) {
        this.client = client;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.tokenStoragePort = tokenStoragePort;
    }

    @Override
    public void createContact(Contact contact) {
        JSONObject properties = new JSONObject();
        properties.put("firstname", contact.getFirstname());
        properties.put("lastname", contact.getLastname());
        properties.put("email", contact.getEmail());

        JSONObject body = new JSONObject();
        body.put("properties", properties);

        String token = tokenStoragePort.getToken();
        if (token == null) {
            throw new HubSpotException("Token de autenticação não disponível");
        }

        Request request = new Request.Builder()
                .url("https://api.hubapi.com/crm/v3/objects/contacts")
                .header("Authorization", "Bearer " + token)
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            int secondlyRemaining = HttpUtils.parseIntHeader(response.header("X-HubSpot-RateLimit-Secondly-Remaining"), 11);
            String dailyRemaining = response.header("X-HubSpot-RateLimit-Daily-Remaining");
            if (response.code() == 429) {
                if (secondlyRemaining == 0) {
                    throw new HubSpotException("Limite de taxa por segundo excedido (11 requisições). Tente novamente em 1 segundo.", "rate_limit_secondly");
                } else if (dailyRemaining != null && Integer.parseInt(dailyRemaining) == 0) {
                    throw new HubSpotException("Limite diário de requisições excedido. Tente novamente amanhã.", "rate_limit_daily");
                } else {
                    throw new HubSpotException("Limite de taxa excedido (provavelmente diário). Tente novamente mais tarde ou amanhã.", "rate_limit_unknown");
                }
            }
            if (!response.isSuccessful()) {
                throw new HubSpotException("Erro ao criar contato: " + response.code());
            }
        } catch (Exception e) {
            throw new HubSpotException("Erro ao chamar API do HubSpot: " + e.getMessage());
        }
    }

    @Override
    public String exchangeCodeForToken(String code) {
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("redirect_uri", redirectUri)
                .add("code", code)
                .build();

        Request request = new Request.Builder()
                .url("https://api.hubapi.com/oauth/v1/token")
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new HubSpotException("Erro ao obter token: " + response.code());
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            return json.getString("access_token");
        } catch (Exception e) {
            throw new HubSpotException("Erro ao chamar API de token: " + e.getMessage());
        }
    }
}