package com.meetime.hubspot.adapter.controller;

import com.meetime.hubspot.application.port.in.WebhookUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    @Autowired
    private WebhookUseCase webhookUseCase;

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody List<Map<String, Object>> events) {
        webhookUseCase.processWebhook(events);
        return ResponseEntity.ok("Webhook processado");
    }
}
