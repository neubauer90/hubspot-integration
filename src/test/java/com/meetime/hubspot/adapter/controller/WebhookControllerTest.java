package com.meetime.hubspot.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetime.hubspot.application.port.in.WebhookUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ExtendWith(MockitoExtension.class)
public class WebhookControllerTest {

    @Mock
    private WebhookUseCase webhookUseCase;

    @InjectMocks
    private WebhookController webhookController;

    private MockMvc mockMvc;
    private String clientSecret = "e149ff69-0abb-4ccc-b29b-368e348a2ce3"; // Alinhado com application.properties

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(webhookController).build();
        Field clientSecretField = WebhookController.class.getDeclaredField("clientSecret");
        clientSecretField.setAccessible(true);
        clientSecretField.set(webhookController, clientSecret);

        // Injeta o ObjectMapper manualmente
        Field objectMapperField = WebhookController.class.getDeclaredField("objectMapper");
        objectMapperField.setAccessible(true);
        objectMapperField.set(webhookController, new ObjectMapper());
    }

    @Test
    public void shouldProcessWebhookWithValidSignature() throws Exception {
        String rawBody = "[{\"subscriptionType\": \"contact.creation\"}]";
        String validSignature = calculateHmacSha256(clientSecret, rawBody);

        mockMvc.perform(post("/webhook")
                        .content(rawBody)
                        .header("X-HubSpot-Signature", validSignature)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook processed successfully"));

        verify(webhookUseCase).processWebhook(any(List.class));
    }

    @Test
    public void shouldRejectWebhookWithInvalidSignature() throws Exception {
        String rawBody = "[{\"subscriptionType\": \"contact.creation\"}]";
        String invalidSignature = "invalid_signature";

        mockMvc.perform(post("/webhook")
                        .content(rawBody)
                        .header("X-HubSpot-Signature", invalidSignature)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid webhook signature"));
    }

    private String calculateHmacSha256(String secret, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}