package com.meetime.hubspot.application.usecase;

import com.meetime.hubspot.application.port.in.WebhookUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WeebhookUseCaseImpl implements WebhookUseCase {

    @Override
    public void processWebhook(List<Map<String, Object>> events) {
        events.forEach(event -> {
            if ("contact.creation".equals(event.get("subscriptionType"))) {
                System.out.println("Novo contato criado: " + event);
            }
        });
    }
}
