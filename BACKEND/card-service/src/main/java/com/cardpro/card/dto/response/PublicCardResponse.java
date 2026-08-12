package com.cardpro.card.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

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
}
