package com.cardpro.lead.client;

import java.util.UUID;

/**
 * Mirrors the card-service {@code CardResponse} returned by the internal
 * card lookup endpoint. {@code profileData} is a JSON string containing the
 * card owner's profile fields (e.g. {@code email}).
 */
public record CardProfileResponse(
    UUID id,
    UUID userId,
    String slug,
    String templateId,
    String profileData,
    String aiAvatarUrl,
    Boolean isActive
) {
}
