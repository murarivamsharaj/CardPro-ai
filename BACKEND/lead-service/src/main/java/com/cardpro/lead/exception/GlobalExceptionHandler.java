package com.cardpro.lead.exception;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Validation Failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\n  \"status\": \"error\",\n  \"error\": {\n    \"code\": \"VALIDATION_FAILED\",\n    \"message\": \"Invalid request parameters\",\n    \"details\": {\n      \"email\": \"must be a well-formed email address\"\n    }\n  },\n  \"timestamp\": \"2026-08-07T12:00:00Z\"\n}")))
    })
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> errorDetail = new HashMap<>();
        errorDetail.put("code", "VALIDATION_FAILED");
        errorDetail.put("message", "Invalid request parameters");
        errorDetail.put("details", fieldErrors);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("error", errorDetail);
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}