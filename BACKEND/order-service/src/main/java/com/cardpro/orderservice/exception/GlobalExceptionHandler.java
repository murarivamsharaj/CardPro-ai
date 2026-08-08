package com.cardpro.orderservice.exception;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Order Not Found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\n  \"status\": \"error\",\n  \"error\": {\n    \"code\": \"ORDER_NOT_FOUND\",\n    \"message\": \"Order not found with ID: 123\",\n    \"details\": {\"orderId\": \"123\"}\n  },\n  \"timestamp\": \"2026-08-07T12:00:00Z\"\n}")))
    })
    public ResponseEntity<Map<String, Object>> handleOrderNotFound(OrderNotFoundException ex) {
        log.warn("Order not found: {}", ex.getMessage());

        Map<String, Object> errorDetail = new HashMap<>();
        errorDetail.put("code", "ORDER_NOT_FOUND");
        errorDetail.put("message", ex.getMessage());
        errorDetail.put("details", Map.of("orderId", ex.getOrderId()));

        return buildErrorResponse(HttpStatus.NOT_FOUND, errorDetail);
    }

    @ExceptionHandler(ProductUnavailableException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Product Unavailable",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\n  \"status\": \"error\",\n  \"error\": {\n    \"code\": \"PRODUCT_UNAVAILABLE\",\n    \"message\": \"Product out of stock\",\n    \"details\": {\"productId\": \"456\"}\n  },\n  \"timestamp\": \"2026-08-07T12:00:00Z\"\n}")))
    })
    public ResponseEntity<Map<String, Object>> handleProductUnavailable(ProductUnavailableException ex) {
        log.warn("Product unavailable: {}", ex.getMessage());

        Map<String, Object> errorDetail = new HashMap<>();
        errorDetail.put("code", "PRODUCT_UNAVAILABLE");
        errorDetail.put("message", ex.getMessage());
        errorDetail.put("details", Map.of("productId", ex.getProductId()));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorDetail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Validation Failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\n  \"status\": \"error\",\n  \"error\": {\n    \"code\": \"VALIDATION_FAILED\",\n    \"message\": \"Invalid request parameters\",\n    \"details\": {\"quantity\": \"must be greater than 0\"}\n  },\n  \"timestamp\": \"2026-08-07T12:00:00Z\"\n}")))
    })
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\n  \"status\": \"error\",\n  \"error\": {\n    \"code\": \"BAD_REQUEST\",\n    \"message\": \"Invalid argument passed\",\n    \"details\": null\n  },\n  \"timestamp\": \"2026-08-07T12:00:00Z\"\n}")))
    })
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        Map<String, Object> errorDetail = new HashMap<>();
        errorDetail.put("code", "BAD_REQUEST");
        errorDetail.put("message", ex.getMessage());
        errorDetail.put("details", null);

        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorDetail);
    }

    @ExceptionHandler(Exception.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\n  \"status\": \"error\",\n  \"error\": {\n    \"code\": \"INTERNAL_ERROR\",\n    \"message\": \"An unexpected error occurred. Please try again later.\",\n    \"details\": null\n  },\n  \"timestamp\": \"2026-08-07T12:00:00Z\"\n}")))
    })
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