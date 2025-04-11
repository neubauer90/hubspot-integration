package com.meetime.hubspot.adapter.controller;

import com.meetime.hubspot.application.port.in.CreateContactUseCase;
import com.meetime.hubspot.config.RateLimitConfig;
import com.meetime.hubspot.domain.entity.Contact;
import com.meetime.hubspot.domain.exception.DailyRateLimitExceededException;
import com.meetime.hubspot.domain.exception.SecondlyRateLimitExceededException;
import io.github.bucket4j.Bucket;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            long waitTimeSeconds = bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill() / 1_000_000_000;
            if (waitTimeSeconds == 1) {
                throw new SecondlyRateLimitExceededException("Local per-second request limit exceeded (11). Try again in 1 second.");
            } else {
                throw new DailyRateLimitExceededException("Local daily request limit exceeded (25,000). Try again tomorrow.");
            }
        }
        createContactUseCase.createContact(contact);
        return ResponseEntity.status(HttpStatus.CREATED).body("Contact created successfully");
    }

    @ExceptionHandler(SecondlyRateLimitExceededException.class)
    public ResponseEntity<String> handleSecondlyRateLimitExceededException(SecondlyRateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
    }

    @ExceptionHandler(DailyRateLimitExceededException.class)
    public ResponseEntity<String> handleDailyRateLimitExceededException(DailyRateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
    }
}