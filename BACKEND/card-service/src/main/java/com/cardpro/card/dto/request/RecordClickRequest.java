package com.cardpro.card.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of the internal click-tracking endpoint. {@code linkLabel} is the
 * human-readable link name stored on the profile (e.g. "LinkedIn", "GitHub").
 */
public record RecordClickRequest(
        @NotBlank(message = "linkLabel is required")
        @Size(max = 100, message = "linkLabel must be at most 100 characters")
        String linkLabel
) {
}
