package com.cardpro.card.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCardRequest {

    @NotBlank(message = "Slug is required")
    private String slug;

    private String templateId;

    @NotBlank(message = "Profile data is required")
    private String profileData;
}
