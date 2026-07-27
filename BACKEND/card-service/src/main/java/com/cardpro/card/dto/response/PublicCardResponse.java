package com.cardpro.card.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PublicCardResponse {
    private String slug;
    private String templateId;
    private String profileData;
    private String aiAvatarUrl;
}
