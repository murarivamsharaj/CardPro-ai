package com.cardpro.card.controller;

import com.cardpro.card.dto.response.CardResponse;
import com.cardpro.card.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards/internal")
@RequiredArgsConstructor
public class InternalCardController {

    private final CardService cardService;

    @GetMapping("/{profileId}")
    public ResponseEntity<CardResponse> getProfileById(@PathVariable UUID profileId) {
        return ResponseEntity.ok(cardService.getCardById(profileId));
    }

    /**
     * Internal counterpart of {@code GET /api/v1/cards/me}. Used by lead-service
     * over Feign (no JWT, only the internal API key) to resolve which cards belong
     * to the logged-in user so it can scope its lead queries. The caller's identity
     * arrives in the {@code X-User-Id} header, which the gateway injects from the
     * JWT and lead-service forwards as-is.
     */
    @GetMapping("/me")
    public ResponseEntity<CardResponse> getMyCardByUserId(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(cardService.getCardByUserId(userId));
    }

    @PostMapping("/{profileId}/increment-view")
    public ResponseEntity<Void> incrementView(@PathVariable UUID profileId) {
        cardService.incrementViewCount(profileId);
        return ResponseEntity.ok().build();
    }
}
