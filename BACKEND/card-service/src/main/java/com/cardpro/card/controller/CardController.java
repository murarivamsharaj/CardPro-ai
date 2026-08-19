package com.cardpro.card.controller;

import com.cardpro.card.dto.request.CreateCardRequest;
import com.cardpro.card.dto.request.UpdateCardRequest;
import com.cardpro.card.dto.response.CardResponse;
import com.cardpro.card.dto.response.PublicCardResponse;
import com.cardpro.card.exception.CardNotFoundException;
import com.cardpro.card.security.CardUserPrincipal;
import com.cardpro.card.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    /**
     * 🔒 PERMANENT SECURITY FIX:
     * Previously leaked all cards in the database. Now strictly locked to the
     * authenticated user's Principal ID. Wraps the secure list in a PageImpl
     * to satisfy the frontend Dashboard without breaking CardService contracts.
     */
    @GetMapping
    public ResponseEntity<Page<CardResponse>> getAllCards(
            Principal principal,
            @RequestParam(required = false, defaultValue = "") String search,
            Pageable pageable
    ) {
        try {
            // Safely fetch ONLY the logged-in user's cards
            List<CardResponse> userCards = cardService.getCardsByUserId(principal.getName());

            // Apply search filter locally if requested
            if (!search.isEmpty()) {
                String keyword = search.toLowerCase();
                userCards = userCards.stream()
                        .filter(c -> c.getSlug() != null && c.getSlug().toLowerCase().contains(keyword))
                        .toList();
            }

            // Wrap the secure list in a Page object to maintain frontend compatibility
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), userCards.size());
            List<CardResponse> pageContent = start <= end ? userCards.subList(start, end) : List.of();

            return ResponseEntity.ok(new PageImpl<>(pageContent, pageable, userCards.size()));

        } catch (CardNotFoundException e) {
            // Graceful degradation: If a new user has no cards, return a clean,
            // empty page so the Dashboard renders "0 cards" instead of crashing.
            return ResponseEntity.ok(new PageImpl<>(List.of(), pageable, 0));
        }
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PublicCardResponse> getPublicCard(@PathVariable String slug) {
        return ResponseEntity.ok(cardService.getPublicCard(slug));
    }

    @GetMapping("/public/{slug}")
    public ResponseEntity<PublicCardResponse> getPublicCardBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(cardService.findBySlug(slug));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<PublicCardResponse> getPublicCardBySlugAlias(@PathVariable String slug) {
        return ResponseEntity.ok(cardService.findBySlug(slug));
    }

    @GetMapping("/me")
    public ResponseEntity<List<CardResponse>> getMyCards(Principal principal) {
        return ResponseEntity.ok(cardService.getCardsByUserId(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<CardResponse> createCard(
            Principal principal,
            @RequestHeader(value = "X-User-Email", required = false) String headerEmail,
            @Valid @RequestBody CreateCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardService.createCard(principal.getName(), resolveOwnerEmail(principal, headerEmail), request));
    }

    @PutMapping("/me")
    public ResponseEntity<CardResponse> updateCard(
            Principal principal,
            @RequestHeader(value = "X-User-Email", required = false) String headerEmail,
            @Valid @RequestBody UpdateCardRequest request) {
        return ResponseEntity.ok(cardService.updateCard(principal.getName(), resolveOwnerEmail(principal, headerEmail), request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCard(Principal principal) {
        cardService.deleteCard(principal.getName());
        return ResponseEntity.noContent().build();
    }

    private String resolveOwnerEmail(Principal principal, String headerEmail) {
        if (principal instanceof CardUserPrincipal cardUser && cardUser.email() != null && !cardUser.email().isBlank()) {
            return cardUser.email();
        }
        return headerEmail;
    }
}