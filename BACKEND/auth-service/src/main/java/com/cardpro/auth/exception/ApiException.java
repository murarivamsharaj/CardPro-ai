package com.cardpro.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for all API errors in the auth-service.
 * Carries a structured error code and HTTP status for consistent error responses
 * matching the SRS standard format:
 *
 * <pre>
 * {
 *   "status": "error",
 *   "error": {
 *     "code": "ERROR_CODE",
 *     "message": "Human-readable message"
 *   },
 *   "timestamp": "2026-07-27T10:30:00Z"
 * }
 * </pre>
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    protected ApiException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
