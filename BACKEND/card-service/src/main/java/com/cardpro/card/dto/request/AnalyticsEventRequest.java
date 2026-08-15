package com.cardpro.card.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Body of the public analytics event endpoint ({@code POST /api/v1/analytics/events}),
 * fired by the public card viewer without any authentication:
 *
 * <ul>
 *   <li>{@code PAGE_VIEW} — a visitor opened the card page (also bumps the
 *       cumulative view counter and contributes to the unique-visitor metric)</li>
 *   <li>{@code SOCIAL_CLICK} — a visitor tapped one of the card's social links</li>
 *   <li>{@code BUTTON_CLICK} — a visitor used a quick action (call / WhatsApp / email)</li>
 *   <li>{@code VCF_DOWNLOAD} — a visitor downloaded the vCard contact file</li>
 * </ul>
 *
 * {@code visitorId} is an anonymized per-browser session id generated on the
 * client (no PII) and is what makes the unique-visitor count meaningful.
 */
public record AnalyticsEventRequest(
        @NotNull(message = "profileId is required")
        UUID profileId,

        @NotBlank(message = "eventType is required")
        @Size(max = 20, message = "eventType must be at most 20 characters")
        String eventType,

        @Size(max = 100, message = "linkLabel must be at most 100 characters")
        String linkLabel,

        @Size(max = 64, message = "visitorId must be at most 64 characters")
        String visitorId
) {
}
