package com.cardpro.card.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class PublicCardResponse {
    private UUID id;
    private String slug;
    private String templateId;
    private String profileData;
    private String aiAvatarUrl;
    private String address;
    private String gender;
    private Map<String, String> socialLinks;
    /**
     * Owner's user-service preference (Pro perk): when true, the card viewer
     * hides the "Powered by CardPro" watermark footer. Defaults to false.
     */
    @Builder.Default
    private boolean removeWatermark = false;
}
