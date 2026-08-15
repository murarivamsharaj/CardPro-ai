package com.cardpro.card.security;

import java.security.Principal;

/**
 * Authenticated principal for card-service requests. {@code getName()} returns
 * the JWT subject (the auth-service user UUID) so all existing
 * {@code UUID.fromString(principal.getName())} call sites keep working, while
 * the {@code email} claim (same JWT, different field) stays available for
 * cross-service lookups — e.g. resolving the owner's user-service preferences.
 */
public record CardUserPrincipal(String userId, String email) implements Principal {

    @Override
    public String getName() {
        return userId;
    }
}
