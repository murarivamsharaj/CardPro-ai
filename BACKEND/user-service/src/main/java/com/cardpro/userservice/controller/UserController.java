package com.cardpro.userservice.controller;

import com.cardpro.userservice.dto.UserRequest;
import com.cardpro.userservice.dto.UserResponse;
import com.cardpro.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "CRUD APIs for managing CardPro users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        UserResponse createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // 🔥 THE FIX: Explicit GET /me endpoint using the correct service method name
    @GetMapping("/me")
    @Operation(summary = "Get authenticated user profile")
    public ResponseEntity<UserResponse> getMyProfile(HttpServletRequest request) {
        String email = extractEmailFromRequest(request);

        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Changed from getUserByEmail to getProfileByEmail
        return ResponseEntity.ok(userService.getProfileByEmail(email));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user profile by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    @Operation(summary = "Get all users (Admin)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing user profile")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user account")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping("/me/upgrade")
    @Operation(summary = "Upgrade the authenticated user to Pro")
    public ResponseEntity<UserResponse> upgradeToPro(HttpServletRequest request) {
        String email = extractEmailFromRequest(request);

        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserResponse response = userService.upgradeToPro(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Extracts user email safely from gateway headers or by decoding the JWT Bearer token.
     */
    private String extractEmailFromRequest(HttpServletRequest request) {
        // 1. Check direct Gateway headers
        String email = request.getHeader("X-User-Email");
        if (email != null && !email.isBlank()) {
            return email.trim();
        }

        email = request.getHeader("X-Auth-User");
        if (email != null && !email.isBlank()) {
            return email.trim();
        }

        // 2. Fallback: Parse Bearer JWT token directly from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String[] chunks = token.split("\\.");
                if (chunks.length >= 2) {
                    String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));

                    // Look for "email":"user@domain.com"
                    String searchStr = "\"email\":\"";
                    int startIndex = payload.indexOf(searchStr);
                    if (startIndex != -1) {
                        startIndex += searchStr.length();
                        int endIndex = payload.indexOf("\"", startIndex);
                        if (endIndex != -1) {
                            return payload.substring(startIndex, endIndex);
                        }
                    }

                    // Fallback to "sub":"user@domain.com"
                    searchStr = "\"sub\":\"";
                    startIndex = payload.indexOf(searchStr);
                    if (startIndex != -1) {
                        startIndex += searchStr.length();
                        int endIndex = payload.indexOf("\"", startIndex);
                        if (endIndex != -1) {
                            return payload.substring(startIndex, endIndex);
                        }
                    }
                }
            } catch (Exception ignored) {
                // If token parsing fails, return null to send 401
            }
        }

        return null;
    }
}