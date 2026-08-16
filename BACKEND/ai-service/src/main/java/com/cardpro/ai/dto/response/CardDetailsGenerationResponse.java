package com.cardpro.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CardDetailsGenerationResponse {

    private String suggestedBio;
    private String suggestedTagline;
    private String suggestedJobTitle;

    /** Which generator produced the result: the Gemini model name or "fallback". */
    private String model;

    /** True when the response came from the hard-coded fallback, not AI. */
    private boolean fallback;
}
