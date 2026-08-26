package com.cardpro.card.controller;

import com.cardpro.card.dto.request.CreateCardRequest;
import com.cardpro.card.dto.request.UpdateCardRequest;
import com.cardpro.card.dto.response.CardResponse;
import com.cardpro.card.dto.response.PublicCardResponse;
import com.cardpro.card.exception.CardNotFoundException;
import com.cardpro.card.service.CardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getAllCards(
            HttpServletRequest httpRequest,
            @RequestParam(required = false, defaultValue = "") String search,
            Pageable pageable
    ) {
        String email = extractEmailFromRequest(httpRequest);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Fetch ONLY the logged-in user's cards using their email/userId
            List<CardResponse> userCards = cardService.getCardsByUserId(email);

            if (!search.isEmpty()) {
                String keyword = search.toLowerCase();
                userCards = userCards.stream()
                        .filter(c -> c.getSlug() != null && c.getSlug().toLowerCase().contains(keyword))
                        .toList();
            }

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), userCards.size());
            List<CardResponse> pageContent = start <= end ? userCards.subList(start, end) : List.of();

            return ResponseEntity.ok(new PageImpl<>(pageContent, pageable, userCards.size()));

        } catch (CardNotFoundException e) {
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
    public ResponseEntity<List<CardResponse>> getMyCards(HttpServletRequest httpRequest) {
        String email = extractEmailFromRequest(httpRequest);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(cardService.getCardsByUserId(email));
    }

    @PostMapping
    public ResponseEntity<CardResponse> createCard(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreateCardRequest request) {

        String email = extractEmailFromRequest(httpRequest);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // We use email as the identifier for both userId mapping and ownerEmail
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardService.createCard(email, email, request));
    }

    @PutMapping("/me")
    public ResponseEntity<CardResponse> updateCard(
            HttpServletRequest httpRequest,
            @Valid @RequestBody UpdateCardRequest request) {

        String email = extractEmailFromRequest(httpRequest);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(cardService.updateCard(email, email, request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCard(HttpServletRequest httpRequest) {
        String email = extractEmailFromRequest(httpRequest);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        cardService.deleteCard(email);
        return ResponseEntity.noContent().build();
    }

    /**
     * Safely extracts the user's email from Gateway headers or parses the JWT token directly.
     * Prevents 403 Forbidden errors when Spring Principal fails to inject.
     */
    private String extractEmailFromRequest(HttpServletRequest request) {
        String email = request.getHeader("X-User-Email");
        if (email != null && !email.isBlank()) {
            return email.trim();
        }

        email = request.getHeader("X-Auth-User");
        if (email != null && !email.isBlank()) {
            return email.trim();
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String[] chunks = token.split("\\.");
                if (chunks.length >= 2) {
                    String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));

                    String searchStr = "\"email\":\"";
                    int startIndex = payload.indexOf(searchStr);
                    if (startIndex != -1) {
                        startIndex += searchStr.length();
                        int endIndex = payload.indexOf("\"", startIndex);
                        if (endIndex != -1) return payload.substring(startIndex, endIndex);
                    }

                    searchStr = "\"sub\":\"";
                    startIndex = payload.indexOf(searchStr);
                    if (startIndex != -1) {
                        startIndex += searchStr.length();
                        int endIndex = payload.indexOf("\"", startIndex);
                        if (endIndex != -1) return payload.substring(startIndex, endIndex);
                    }
                }
            } catch (Exception ignored) { }
        }
        return null;
    }
}