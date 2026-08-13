package com.cardpro.card.dto.response;

/**
 * Payload for the admin metrics endpoint: the absolute total number of digital
 * cards in the system, regardless of owner.
 */
public record TotalCardsResponse(long totalCards) {
}
