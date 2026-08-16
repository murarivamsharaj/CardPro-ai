package com.cardpro.ai.controller;

import com.cardpro.ai.dto.request.BioGenerationRequest;
import com.cardpro.ai.dto.request.CardDetailsGenerationRequest;
import com.cardpro.ai.dto.response.BioGenerationResponse;
import com.cardpro.ai.dto.response.CardDetailsGenerationResponse;
import com.cardpro.ai.service.BioGenerationService;
import com.cardpro.ai.service.CardDetailsGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final BioGenerationService bioGenerationService;
    private final CardDetailsGenerationService cardDetailsGenerationService;

    @PostMapping("/generate-bio")
    public ResponseEntity<BioGenerationResponse> generateBio(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody BioGenerationRequest request) {
        return ResponseEntity.ok(bioGenerationService.generateBio(userId, request));
    }

    /**
     * Magic Autofill: given keywords / a rough summary, returns AI-suggested
     * bio, tagline, and job title for the card editor form.
     */
    @PostMapping("/generate-card-details")
    public ResponseEntity<CardDetailsGenerationResponse> generateCardDetails(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CardDetailsGenerationRequest request) {
        return ResponseEntity.ok(cardDetailsGenerationService.generateCardDetails(userId, request));
    }
}
