package com.cardpro.ai.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OpenAiClient {

    private final WebClient webClient;

    public OpenAiClient(WebClient openAiWebClient) {
        this.webClient = openAiWebClient;
    }

    public String generateBio(String prompt) {
        // Implementation using OpenAI Chat Completions API
        return "";
    }

    public String generateLeadFollowup(String visitorName, String profileOwnerName) {
        // Implementation for WhatsApp follow-up generation
        return "";
    }

    /**
     * Generates card-detail suggestions (bio / tagline / job title) from a
     * keyword prompt. Placeholder until the OpenAI Chat Completions call is
     * implemented — callers currently fall back to the mock generator.
     */
    public String generateCardDetails(String prompt, String tone) {
        // Implementation using OpenAI Chat Completions API
        return null;
    }
}
