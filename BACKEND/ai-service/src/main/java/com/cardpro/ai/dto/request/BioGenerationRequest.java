package com.cardpro.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BioGenerationRequest {

    @NotBlank(message = "Raw notes are required")
    private String rawNotes;

    private String tone;
}
