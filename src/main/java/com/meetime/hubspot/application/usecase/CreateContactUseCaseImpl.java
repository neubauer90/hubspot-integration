package com.meetime.hubspot.application.usecase;

import com.meetime.hubspot.application.port.in.CreateContactUseCase;
import com.meetime.hubspot.application.port.out.ExternalApiPort;
import com.meetime.hubspot.domain.entity.Contact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateContactUseCaseImpl implements CreateContactUseCase {

    @Autowired
    private ExternalApiPort externalApiPort;

    @Override
    public void createContact(Contact contact) {
        externalApiPort.createContact(contact);
    }
}
