package com.meetime.hubspot.application.port.in;

import java.util.List;
import java.util.Map;

public interface WebhookUseCase {
    void processWebhook(List<Map<String, Object>> events);
}
