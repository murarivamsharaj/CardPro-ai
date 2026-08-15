package com.cardpro.card.controller;

import com.cardpro.card.dto.request.RecordClickRequest;
import com.cardpro.card.dto.response.CardResponse;
import com.cardpro.card.service.CardService;
import jakarta.validation.Valid;
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

    /**
     * Bumps the profile's cumulative view counter and appends a VIEW event to
     * the analytics log. {@code visitorId} is optional: when supplied (e.g. a
     * session id from the card viewer), the analytics dashboard can report
     * unique visitors instead of raw impressions.
     */
    @PostMapping("/{profileId}/increment-view")
    public ResponseEntity<Void> incrementView(
            @PathVariable UUID profileId,
            @RequestParam(required = false) String visitorId) {
        cardService.incrementViewCount(profileId, visitorId);
        return ResponseEntity.ok().build();
    }

    /**
     * Records a CLICK event for the analytics dashboard (per-link performance).
     * Callers are internal integrations (e.g. a service tracking taps on the
     * public viewer's social links).
     */
    @PostMapping("/{profileId}/increment-click")
    public ResponseEntity<Void> incrementClick(
            @PathVariable UUID profileId,
            @Valid @RequestBody RecordClickRequest request,
            @RequestParam(required = false) String visitorId) {
        cardService.recordClick(profileId, request.linkLabel(), visitorId);
        return ResponseEntity.ok().build();
    }
}
