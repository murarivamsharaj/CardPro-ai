package com.cardpro.card.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Map;

@Data
public class UpdateCardRequest {

    @Size(min = 3, max = 100, message = "Slug must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug can only contain lowercase letters, numbers, and hyphens")
    private String slug;

    private String templateId;

    private Map<String, Object> profileData; // Changed from String to Map for JSONB

    private Boolean isActive;
}