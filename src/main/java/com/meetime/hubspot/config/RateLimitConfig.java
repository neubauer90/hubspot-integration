package com.meetime.hubspot.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Bean
    public Bucket hubSpotRateLimiter() {
        // Limite de 11 requisições por segundo
        Bandwidth secondlyLimit = Bandwidth.classic(11, Refill.greedy(11, Duration.ofSeconds(1)));
        // Limite diário de 25.000
        Bandwidth dailyLimit = Bandwidth.classic(25000, Refill.greedy(25000, Duration.ofDays(1)));
        return Bucket.builder()
                .addLimit(secondlyLimit)
                .addLimit(dailyLimit)
                .build();
    }
}
