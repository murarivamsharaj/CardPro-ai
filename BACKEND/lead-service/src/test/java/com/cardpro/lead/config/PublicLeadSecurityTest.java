package com.cardpro.lead.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression guards for the public lead capture flow: an unauthenticated
 * visitor submitting the "Contact Me" form must reach the controller instead
 * of being rejected with 403 Forbidden, and error dispatches must surface
 * their real status codes (404/400) rather than being masked as 403.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.mail.username=test@cardpro.example",
    "spring.mail.password=test",
    "app.internal.api-key=test-internal-key"
})
class PublicLeadSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicVisitorCanSubmitLeadWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "profileId": "%s",
                      "visitorName": "Public Visitor",
                      "visitorEmail": "visitor@example.com",
                      "message": "Hello from the public lead form"
                    }
                    """.formatted(UUID.randomUUID())))
            .andExpect(status().isCreated());
    }

    @Test
    void internalLeadPathIsPermittedWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/leads/internal/credits/deduct")
                .param("userId", "user-123"))
            .andExpect(status().isOk());
    }

    @Test
    void unknownPathReturnsReal404InsteadOf403() throws Exception {
        // This path is permitAll'd (internal/**) but has no handler — the
        // resulting 404 must surface via the error dispatch, not a 403.
        mockMvc.perform(get("/api/v1/leads/internal/no-such-route"))
            .andExpect(status().isNotFound());
    }

    @Test
    void invalidLeadBodyReturns400InsteadOf403() throws Exception {
        mockMvc.perform(post("/api/v1/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "visitorName": "No Profile Id",
                      "visitorEmail": "visitor@example.com"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }
}
