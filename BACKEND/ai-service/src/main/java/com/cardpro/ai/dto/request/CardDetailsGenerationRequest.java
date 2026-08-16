package com.cardpro.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CardDetailsGenerationRequest {

    /**
     * Free-form keywords or a rough summary of the user's role, industry,
     * and experience — e.g. "sales manager fintech SaaS".
     */
    @NotBlank(message = "Prompt is required")
    private String prompt;

    /** Optional creative direction, e.g. "friendly", "premium", "technical". */
    private String tone;
}
