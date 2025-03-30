package com.meetime.hubspot.adapter.controller;

import com.meetime.hubspot.application.port.in.CreateContactUseCase;
import com.meetime.hubspot.config.RateLimitConfig;
import com.meetime.hubspot.domain.entity.Contact;
import com.meetime.hubspot.domain.exception.DailyRateLimitExceededException;
import com.meetime.hubspot.domain.exception.SecondlyRateLimitExceededException;
import io.github.bucket4j.Bucket;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    private final CreateContactUseCase createContactUseCase;

    private final RateLimitConfig rateLimitConfig;

    public ContactController(CreateContactUseCase createContactUseCase, RateLimitConfig rateLimitConfig) {
        this.createContactUseCase = createContactUseCase;
        this.rateLimitConfig = rateLimitConfig;
    }

    @PostMapping
    public ResponseEntity<String> createContact(@RequestBody Contact contact) {
        Bucket bucket = rateLimitConfig.hubSpotRateLimiter();
        if (!bucket.tryConsume(1)) {
            // Verifica o tempo de espera para cada limite
            long secondlyWait = bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill() / 1_000_000_000; // segundos
            if (secondlyWait == 1) {
                throw new SecondlyRateLimitExceededException("Limite local de requisições por segundo excedido (11). Tente novamente em 1 segundo.");
            } else {
                throw new DailyRateLimitExceededException("Limite local diário de requisições excedido (25.000). Tente novamente amanhã.");
            }
        }
        createContactUseCase.createContact(contact);
        return ResponseEntity.status(HttpStatus.CREATED).body("Contato criado com sucesso");
    }
}
