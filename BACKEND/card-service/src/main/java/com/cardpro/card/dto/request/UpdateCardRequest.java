package com.cardpro.card.dto.request;

import lombok.Data;

@Data
public class UpdateCardRequest {
    private String slug;
    private String templateId;
    private String profileData;
    private Boolean isActive;
}
