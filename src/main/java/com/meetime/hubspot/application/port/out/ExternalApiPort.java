package com.meetime.hubspot.application.port.out;

import com.meetime.hubspot.domain.entity.Contact;

public interface ExternalApiPort {
    void createContact(Contact contact);
    String exchangeCodeForToken(String code);
}
