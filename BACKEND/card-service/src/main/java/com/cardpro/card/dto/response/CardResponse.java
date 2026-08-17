package com.cardpro.card.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class CardResponse {
    private UUID id;
    private UUID userId;
    private String slug;
    private String templateId;
    private String profileData;
    private String aiAvatarUrl;
    private String address;
    private String gender;
    private Map<String, String> socialLinks;
    private Boolean isActive;
    /** True when the user has unlocked the Premium Templates entitlement. */
    @Builder.Default
    private boolean premiumTemplatesUnlocked = false;

    /** Consumable lead-credit balance (Lead Pack purchases). */
    @Builder.Default
    private int leadCredits = 0;
}
