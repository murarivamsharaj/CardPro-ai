package com.cardpro.ai.service;

import com.cardpro.ai.client.LeadServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeadFollowupService {

    private final LeadServiceClient leadServiceClient;

    @Value("${app.ai.fallback.lead-followup}")
    private String fallbackMessage;

    // ADDED: Inject the internal API key from your application properties
    @Value("${app.internal.api-key}")
    private String internalApiKey;

    public void generateFollowup(String leadId) {
        try {
            // Call OpenAI to generate WhatsApp template
            String followup = "Hi {visitor_name}! Thanks for your interest.";

            // FIXED: Pass the internalApiKey as the required third argument
            leadServiceClient.updateFollowup(leadId, followup, internalApiKey);
        } catch (Exception e) {
            // Use fallback
        }
    }
}