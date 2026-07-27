package com.cardpro.ai.service;

import com.cardpro.ai.dto.request.BioGenerationRequest;
import com.cardpro.ai.dto.response.BioGenerationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BioGenerationService {

    @Value("${app.ai.fallback.bio}")
    private String fallbackBio;

    public BioGenerationResponse generateBio(String userId, BioGenerationRequest request) {
        try {
            // Call OpenAI API via OpenAiClient
            // String generatedBio = openAiClient.generateBio(request.getRawNotes());
            String generatedBio = "Generated professional bio based on: " + request.getRawNotes();

            return BioGenerationResponse.builder()
                .bio(generatedBio)
                .model("gpt-4")
                .fallback(false)
                .build();
        } catch (Exception e) {
            return BioGenerationResponse.builder()
                .bio(fallbackBio)
                .model("fallback")
                .fallback(true)
                .build();
        }
    }
}
