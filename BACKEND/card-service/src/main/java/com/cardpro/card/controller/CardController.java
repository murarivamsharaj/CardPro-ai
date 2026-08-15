package com.cardpro.card.controller;

import com.cardpro.card.dto.request.CreateCardRequest;
import com.cardpro.card.dto.request.UpdateCardRequest;
import com.cardpro.card.dto.response.CardResponse;
import com.cardpro.card.dto.response.PublicCardResponse;
import com.cardpro.card.security.CardUserPrincipal;
import com.cardpro.card.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // 👈 1. Added Principal import

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @RequestParam(required = false, defaultValue = "") String search,
            Pageable pageable
    ) {
        Page<CardResponse> cards;

        if (search.isEmpty()) {
            cards = cardService.getAllCards(pageable);
        } else {
            cards = cardService.searchCards(search, pageable);
        }

        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PublicCardResponse> getPublicCard(@PathVariable String slug) {
        return ResponseEntity.ok(cardService.getPublicCard(slug));
    }

    /**
     * Dedicated public slug endpoint (unauthenticated). Resolves an ACTIVE card
     * by slug via {@code findBySlug} — deactivated cards return 404.
     */
    @GetMapping("/public/{slug}")
    public ResponseEntity<PublicCardResponse> getPublicCardBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(cardService.findBySlug(slug));
    }

    /** Alias of {@code /public/{slug}} kept for clients using the /slug/ path. */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<PublicCardResponse> getPublicCardBySlugAlias(@PathVariable String slug) {
        return ResponseEntity.ok(cardService.findBySlug(slug));
    }

    // 👇 2. Changed from @RequestHeader to Principal
    @GetMapping("/me")
    public ResponseEntity<CardResponse> getMyCard(Principal principal) {
        return ResponseEntity.ok(cardService.getCardByUserId(principal.getName()));
    }

    // 👇 3. Changed from @RequestHeader to Principal
    @PostMapping
    public ResponseEntity<CardResponse> createCard(
            Principal principal,
            @RequestHeader(value = "X-User-Email", required = false) String headerEmail,
            @Valid @RequestBody CreateCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardService.createCard(principal.getName(), resolveOwnerEmail(principal, headerEmail), request));
    }

    // 👇 4. Changed from @RequestHeader to Principal
    @PutMapping("/me")
    public ResponseEntity<CardResponse> updateCard(
            Principal principal,
            @RequestHeader(value = "X-User-Email", required = false) String headerEmail,
            @Valid @RequestBody UpdateCardRequest request) {
        return ResponseEntity.ok(cardService.updateCard(principal.getName(), resolveOwnerEmail(principal, headerEmail), request));
    }

    // 👇 5. Changed from @RequestHeader to Principal
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCard(Principal principal) {
        cardService.deleteCard(principal.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * The owner's account email, used to look up user-service preferences on
     * the public card. Prefers the JWT email claim (set by our filter via
     * {@link CardUserPrincipal}); falls back to the gateway-injected
     * {@code X-User-Email} header for callers that arrive without it.
     */
    private String resolveOwnerEmail(Principal principal, String headerEmail) {
        if (principal instanceof CardUserPrincipal cardUser && cardUser.email() != null && !cardUser.email().isBlank()) {
            return cardUser.email();
        }
        return headerEmail;
    }
}