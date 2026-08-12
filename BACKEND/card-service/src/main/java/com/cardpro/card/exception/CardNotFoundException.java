package com.cardpro.card.exception;

/**
 * Thrown when a card profile cannot be found (by user id, slug, or id).
 * Mapped to HTTP 404 by {@link GlobalExceptionHandler} — the previous
 * unhandled {@link RuntimeException} surfaced as 500, which prevented
 * clients (e.g. GET /api/v1/cards/me) from distinguishing "no card yet"
 * from a genuine server error.
 */
public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException() {
        super("Card profile not found");
    }
}
