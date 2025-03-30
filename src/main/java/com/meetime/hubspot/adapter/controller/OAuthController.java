package com.meetime.hubspot.adapter.controller;

import com.meetime.hubspot.application.port.in.OAuthUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/oauth")
public class OAuthController {

    @Autowired
    private OAuthUseCase oAuthUseCase;

    @GetMapping("/authorize")
    public RedirectView getAuthorizationUrl() {
        return new RedirectView(oAuthUseCase.getAuthorizationUrl());
    }

    @GetMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestParam("code") String code) {
        oAuthUseCase.handleCallback(code);
        return ResponseEntity.ok("Autenticação realizada com sucesso");
    }
}
