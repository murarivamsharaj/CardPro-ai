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

    public void generateFollowup(String leadId) {
        try {
            // Call OpenAI to generate WhatsApp template
            String followup = "Hi {visitor_name}! Thanks for your interest.";
            // Send follow-up back to lead-service via Feign client
            leadServiceClient.updateFollowup(leadId, followup);
        } catch (Exception e) {
            // Use fallback
        }
    }
}
