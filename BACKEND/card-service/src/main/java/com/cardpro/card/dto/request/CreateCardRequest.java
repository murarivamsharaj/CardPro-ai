package com.cardpro.card.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Map;

@Data
public class CreateCardRequest {

    @NotBlank(message = "Slug is required")
    @Size(min = 3, max = 100, message = "Slug must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug can only contain lowercase letters, numbers, and hyphens")
    private String slug;

    private String templateId = "basic"; // Default value per SRS Section 10.2

    @NotNull(message = "Profile data is required")
    private Map<String, Object> profileData; // Maps cleanly to JSONB column
}

