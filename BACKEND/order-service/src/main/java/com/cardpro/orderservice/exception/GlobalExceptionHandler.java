package com.cardpro.orderservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the order-service.
 * Catches all exceptions and returns structured JSON responses
 * consistent with the CardPro AI API contract.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOrderNotFound(OrderNotFoundException ex) {
        log.warn("Order not found: {}", ex.getMessage());

        Map<String, Object> errorDetail = new HashMap<>();
        errorDetail.put("code", "ORDER_NOT_FOUND");
        errorDetail.put("message", ex.getMessage());
        errorDetail.put("details", Map.of("orderId", ex.getOrderId()));

        return buildErrorResponse(HttpStatus.NOT_FOUND, errorDetail);
    }

    @ExceptionHandler(ProductUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleProductUnavailable(ProductUnavailableException ex) {
        log.warn("Product unavailable: {}", ex.getMessage());

        Map<String, Object> errorDetail = new HashMap<>();
        errorDetail.put("code", "PRODUCT_UNAVAILABLE");
        errorDetail.put("message", ex.getMessage());
        errorDetail.put("details", Map.of("productId", ex.getProductId()));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorDetail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.debug("Validation failed: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> errorDetail = new HashMap<>();
        errorDetail.put("code", "VALIDATION_FAILED");
        errorDetail.put("message", "Invalid request parameters");
        errorDetail.put("details", fieldErrors);

        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorDetail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        Map<String, Object> errorDetail = new HashMap<>();
        errorDetail.put("code", "BAD_REQUEST");
        errorDetail.put("message", ex.getMessage());
        errorDetail.put("details", null);

        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);

        Map<String, Object> errorDetail = new HashMap<>();
        errorDetail.put("code", "INTERNAL_ERROR");
        errorDetail.put("message", "An unexpected error occurred. Please try again later.");
        errorDetail.put("details", null);

        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, errorDetail);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, Map<String, Object> errorDetail) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("error", errorDetail);
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.status(status).body(response);
    }
}
