package com.cardpro.card.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

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
    private Boolean isActive;
}
