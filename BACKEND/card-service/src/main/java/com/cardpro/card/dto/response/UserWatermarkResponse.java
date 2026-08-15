package com.cardpro.card.dto.response;

/**
 * Minimal mirror of user-service's {@code WatermarkResponse} — the owner's
 * removeWatermark Pro preference, fetched over Feign when rendering a public
 * card. Kept deliberately tiny: it must never leak other profile data.
 */
public record UserWatermarkResponse(boolean removeWatermark) {
}
