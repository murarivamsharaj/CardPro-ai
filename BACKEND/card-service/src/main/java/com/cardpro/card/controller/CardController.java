package com.cardpro.card.controller;

import com.cardpro.card.dto.request.CreateCardRequest;
import com.cardpro.card.dto.request.UpdateCardRequest;
import com.cardpro.card.dto.response.CardResponse;
import com.cardpro.card.dto.response.PublicCardResponse;
import com.cardpro.card.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping("/{slug}")
    public ResponseEntity<PublicCardResponse> getPublicCard(@PathVariable String slug) {
        return ResponseEntity.ok(cardService.getPublicCard(slug));
    }

    @GetMapping("/me")
    public ResponseEntity<CardResponse> getMyCard(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(cardService.getCardByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<CardResponse> createCard(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(cardService.createCard(userId, request));
    }

    @PutMapping("/me")
    public ResponseEntity<CardResponse> updateCard(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateCardRequest request) {
        return ResponseEntity.ok(cardService.updateCard(userId, request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCard(@RequestHeader("X-User-Id") String userId) {
        cardService.deleteCard(userId);
        return ResponseEntity.noContent().build();
    }
}
