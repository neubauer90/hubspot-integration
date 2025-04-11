package com.meetime.hubspot.adapter.controller;

import com.meetime.hubspot.application.port.in.OAuthUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/oauth")
public class OAuthController {

    @Autowired
    private OAuthUseCase oAuthUseCase;

    private final ConcurrentHashMap<String, String> stateStore = new ConcurrentHashMap<>();

    @GetMapping("/authorize")
    public RedirectView getAuthorizationUrl() {
        String state = UUID.randomUUID().toString();
        stateStore.put(state, state); // Armazena o state como chave e valor (apenas para rastrear)
        String authorizationUrl = oAuthUseCase.getAuthorizationUrl(state);
        return new RedirectView(authorizationUrl);
    }

    @GetMapping("/callback")
    public ResponseEntity<String> handleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String receivedState) {
        String storedState = stateStore.get(receivedState);
        if (storedState == null || !storedState.equals(receivedState)) {
            return ResponseEntity.status(403).body("Invalid state parameter - possible CSRF attack");
        }
        oAuthUseCase.handleCallback(code);
        stateStore.remove(receivedState); // Remove após uso
        return ResponseEntity.ok("Authentication completed successfully");
    }
}