package com.meetime.hubspot.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Contact {
    private final String firstname;
    private final String lastname;
    private final String email;
}
