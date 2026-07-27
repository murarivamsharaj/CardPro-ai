package com.cardpro.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BioGenerationResponse {
    private String bio;
    private String model;
    private boolean fallback;
}
