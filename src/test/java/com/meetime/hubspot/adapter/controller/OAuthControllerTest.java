package com.meetime.hubspot.adapter.controller;

import com.meetime.hubspot.application.port.in.OAuthUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ExtendWith(MockitoExtension.class)
public class OAuthControllerTest {

    @Mock
    private OAuthUseCase oAuthUseCase;

    @InjectMocks
    private OAuthController oAuthController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(oAuthController).build();
    }

    @Test
    public void shouldRedirectToAuthorizationUrlWithState() throws Exception {
        String authUrlBase = "https://app.hubspot.com/oauth/authorize?state=test_state";
        when(oAuthUseCase.getAuthorizationUrl(anyString())).thenReturn(authUrlBase);

        mockMvc.perform(get("/oauth/authorize"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://app.hubspot.com/oauth/authorize?state=*")); // Apenas state
    }

    @Test
    public void shouldHandleCallbackWithValidState() throws Exception {
        String state = "test_state";
        java.lang.reflect.Field stateStoreField = OAuthController.class.getDeclaredField("stateStore");
        stateStoreField.setAccessible(true);
        Map<String, String> stateStore = (Map<String, String>) stateStoreField.get(oAuthController);
        stateStore.put(state, state);

        mockMvc.perform(get("/oauth/callback")
                        .param("code", "test_code")
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(content().string("Authentication completed successfully"));
    }

    @Test
    public void shouldRejectCallbackWithInvalidState() throws Exception {
        String state = "test_state";
        java.lang.reflect.Field stateStoreField = OAuthController.class.getDeclaredField("stateStore");
        stateStoreField.setAccessible(true);
        Map<String, String> stateStore = (Map<String, String>) stateStoreField.get(oAuthController);
        stateStore.put(state, state);

        mockMvc.perform(get("/oauth/callback")
                        .param("code", "test_code")
                        .param("state", "wrong_state"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Invalid state parameter - possible CSRF attack"));
    }
}