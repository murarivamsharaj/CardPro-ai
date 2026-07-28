package com.cardpro.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Standard error response matching the CardPro AI API contract.
 *
 * <pre>
 * {
 *   "status": "error",
 *   "error": {
 *     "code": "ERROR_CODE",
 *     "message": "Human-readable message",
 *     "details": {}
 *   },
 *   "timestamp": "2026-07-27T10:30:00Z"
 * }
 * </pre>
 */
@Data
@Builder
@AllArgsConstructor
public class ErrorResponse {

    @Builder.Default
    private final String status = "error";

    private final ErrorDetail error;

    @Builder.Default
    private final String timestamp = Instant.now().toString();

    @Data
    @Builder
    @AllArgsConstructor
    public static class ErrorDetail {
        private final String code;
        private final String message;
        private final Object details;
    }
}
