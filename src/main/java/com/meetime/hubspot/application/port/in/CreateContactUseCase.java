package com.meetime.hubspot.application.port.in;

import com.meetime.hubspot.domain.entity.Contact;

public interface CreateContactUseCase {
    void createContact(Contact contact);
}
