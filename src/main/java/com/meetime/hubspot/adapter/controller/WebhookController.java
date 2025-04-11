package com.meetime.hubspot.adapter.controller;

import com.meetime.hubspot.application.port.in.WebhookUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger; // Adicionado para debug

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger logger = Logger.getLogger(WebhookController.class.getName());

    @Value("${hubspot.client-secret}")
    private String clientSecret;

    @Autowired
    private WebhookUseCase webhookUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader("X-HubSpot-Signature") String receivedSignature) {
        if (!isValidSignature(rawBody, receivedSignature)) {
            return ResponseEntity.status(401).body("Invalid webhook signature");
        }

        try {
            logger.info("Deserializing rawBody: " + rawBody);
            List<Map<String, Object>> events = objectMapper.readValue(rawBody,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            logger.info("Events deserialized: " + events);
            webhookUseCase.processWebhook(events);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            logger.severe("Failed to deserialize rawBody: " + e.getMessage());
            return ResponseEntity.status(400).body("Failed to process webhook payload");
        }
    }

    private boolean isValidSignature(String rawBody, String receivedSignature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    clientSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = bytesToHex(hashBytes);
            return calculatedSignature.equals(receivedSignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}