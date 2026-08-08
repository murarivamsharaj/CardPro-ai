package com.cardpro.auth.controller;

import com.cardpro.auth.dto.response.UserResponse;
import com.cardpro.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/internal")
@RequiredArgsConstructor
@Tag(name = "internal-auth-controller", description = "Internal APIs meant only for microservice-to-microservice communication")
public class InternalAuthController {

    private final AuthService authService;

    @Operation(summary = "Get user details by ID")
    @Parameter(name = "X-Internal-Api-Key", in = ParameterIn.HEADER, required = true, description = "Must provide the internal secret key", example = "cardpro-secret-key")
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    @Operation(summary = "Get user details by Email")
    @Parameter(name = "X-Internal-Api-Key", in = ParameterIn.HEADER, required = true, description = "Must provide the internal secret key", example = "cardpro-secret-key")
    @GetMapping("/users/by-email")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
        return ResponseEntity.ok(authService.getUserByEmail(email));
    }
}