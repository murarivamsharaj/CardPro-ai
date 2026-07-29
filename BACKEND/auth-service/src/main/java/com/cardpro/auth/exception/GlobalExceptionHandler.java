package com.cardpro.auth.exception;

import com.cardpro.auth.dto.response.ErrorResponse;
import com.cardpro.auth.dto.response.ErrorResponse.ErrorDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the auth-service.
 * Catches all exceptions and returns structured JSON responses
 * matching the CardPro AI API contract:
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
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ──────────────────────────────────────────────
    // Domain Exceptions (ApiException hierarchy)
    // ──────────────────────────────────────────────

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        log.warn("API exception: {} — {}", ex.getErrorCode(), ex.getMessage());
        return buildErrorResponse(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Authentication failure: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage(), null);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        log.warn("Registration conflict: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", ex.getMessage(),
            Map.of("field", "email"));
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(TokenExpiredException ex) {
        log.warn("Token expired: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getErrorCode(), ex.getMessage(), null);
    }

    // ──────────────────────────────────────────────
    // Validation Exceptions
    // ──────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fieldError -> fieldError.getDefaultMessage() != null
                    ? fieldError.getDefaultMessage()
                    : "Invalid value",
                (existing, replacement) -> existing  // Keep first error per field
            ));
        log.debug("Validation failed: {}", fieldErrors);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
            "Request validation failed", fieldErrors);
    }

    // ──────────────────────────────────────────────
    // Spring Security Exceptions
    // ──────────────────────────────────────────────

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication exception: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
            "Authentication failed: " + ex.getMessage(), null);
    }

    // ──────────────────────────────────────────────
    // Fallback
    // ──────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
            "An unexpected error occurred. Please try again later.", null);
    }

    // ──────────────────────────────────────────────
    // Builder
    // ──────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status, String code, String message, Object details) {

        ErrorResponse.ErrorDetail errorDetail = ErrorDetail.builder()
            .code(code)
            .message(message)
            .details(details)
            .build();

        ErrorResponse response = ErrorResponse.builder()
            .error(errorDetail)
            .build();

        return ResponseEntity.status(status).body(response);
    }
}
