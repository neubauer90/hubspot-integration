package com.meetime.hubspot.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetime.hubspot.application.port.in.CreateContactUseCase;
import com.meetime.hubspot.config.RateLimitConfig;
import com.meetime.hubspot.domain.entity.Contact;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.EstimationProbe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateContactUseCase createContactUseCase;

    @Mock
    private RateLimitConfig rateLimitConfig;

    @InjectMocks
    private ContactController contactController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Bucket bucket;

    @BeforeEach
    void setUp() {
        bucket = mock(Bucket.class);
        when(rateLimitConfig.hubSpotRateLimiter()).thenReturn(bucket);
        mockMvc = MockMvcBuilders.standaloneSetup(contactController).build();
    }

    @Test
    void shouldCreateContactSuccessfully() throws Exception {
        // Arrange
        Contact contact = new Contact("John", "Doe", "john.doe@example.com");
        when(bucket.tryConsume(1)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contact)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Contact created successfully"));

        // Verifica que o método foi chamado com um Contact com os mesmos valores
        verify(createContactUseCase, times(1)).createContact(argThat(c ->
                c.getFirstname().equals("John") &&
                        c.getLastname().equals("Doe") &&
                        c.getEmail().equals("john.doe@example.com")
        ));
    }

    @Test
    void shouldThrowSecondlyRateLimitExceededException() throws Exception {
        // Arrange
        Contact contact = new Contact("John", "Doe", "john.doe@example.com");
        when(bucket.tryConsume(1)).thenReturn(false);
        EstimationProbe probe = mock(EstimationProbe.class);
        when(bucket.estimateAbilityToConsume(1)).thenReturn(probe);
        when(probe.getNanosToWaitForRefill()).thenReturn(1_000_000_000L); // 1 segundo

        // Act & Assert
        mockMvc.perform(post("/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contact)))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("Local per-second request limit exceeded (11). Try again in 1 second."));

        verify(createContactUseCase, never()).createContact(any());
    }

    @Test
    void shouldThrowDailyRateLimitExceededException() throws Exception {
        // Arrange
        Contact contact = new Contact("John", "Doe", "john.doe@example.com");
        when(bucket.tryConsume(1)).thenReturn(false);
        EstimationProbe probe = mock(EstimationProbe.class);
        when(bucket.estimateAbilityToConsume(1)).thenReturn(probe);
        when(probe.getNanosToWaitForRefill()).thenReturn(86_400_000_000_000L); // 1 dia

        // Act & Assert
        mockMvc.perform(post("/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contact)))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("Local daily request limit exceeded (25,000). Try again tomorrow."));

        verify(createContactUseCase, never()).createContact(any());
    }
}