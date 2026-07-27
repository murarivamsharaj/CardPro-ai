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

    @PostMapping("/{profileId}/increment-view")
    public ResponseEntity<Void> incrementView(@PathVariable UUID profileId) {
        cardService.incrementViewCount(profileId);
        return ResponseEntity.ok().build();
    }
}
