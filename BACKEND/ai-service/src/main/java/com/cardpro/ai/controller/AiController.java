package com.cardpro.ai.controller;

import com.cardpro.ai.dto.request.BioGenerationRequest;
import com.cardpro.ai.dto.response.BioGenerationResponse;
import com.cardpro.ai.service.BioGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final BioGenerationService bioGenerationService;

    @PostMapping("/generate-bio")
    public ResponseEntity<BioGenerationResponse> generateBio(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody BioGenerationRequest request) {
        return ResponseEntity.ok(bioGenerationService.generateBio(userId, request));
    }
}
