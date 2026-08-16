package com.cardpro.ai.exception;

/**
 * Thrown when the AI card-detail generation fails (API error, timeout, or
 * unparseable model output). Mapped to an HTTP 500 by
 * {@link GlobalExceptionHandler} so callers never receive fake placeholder
 * suggestions.
 */
public class CardGenerationException extends RuntimeException {

    public CardGenerationException(String message) {
        super(message);
    }

    public CardGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
