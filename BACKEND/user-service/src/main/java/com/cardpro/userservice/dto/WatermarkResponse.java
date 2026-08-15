package com.cardpro.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for the internal watermark lookup consumed by card-service when it
 * renders a public card. Kept deliberately tiny — it must not leak any other
 * profile data to an unauthenticated card render.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkResponse {
    /** True when the card owner's "Remove watermark" Pro preference is on. */
    private boolean removeWatermark;
}
