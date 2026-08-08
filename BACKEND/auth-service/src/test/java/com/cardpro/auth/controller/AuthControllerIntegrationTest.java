package com.cardpro.auth.controller;

import com.cardpro.auth.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsAreInvalid() {
        String invalidLoginJson = "{\"email\": \"wrong@example.com\", \"password\": \"badpassword\"}";

        // MockMvc wraps unhandled exceptions in a standard Exception during perform()
        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidLoginJson));
        });

        // Verify the root cause is exactly our custom InvalidCredentialsException
        assertTrue(exception.getCause() instanceof InvalidCredentialsException);
    }
}