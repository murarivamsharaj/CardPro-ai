package com.cardpro.userservice.controller;

import com.cardpro.userservice.dto.UserRequest;
import com.cardpro.userservice.dto.UserResponse;
import com.cardpro.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public ResponseEntity<UserResponse> upgradeToPro(
            @RequestHeader(value = "X-User-Email", required = false) String headerEmail,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String email = headerEmail;

        // Bulletproof fallback: manually extract email from the JWT token if the Gateway header is missing
        if ((email == null || email.isBlank()) && authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String[] chunks = token.split("\\.");
                if (chunks.length >= 2) {
                    String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));
                    String searchStr = "\"email\":\"";
                    int startIndex = payload.indexOf(searchStr);
                    if (startIndex != -1) {
                        startIndex += searchStr.length();
                        int endIndex = payload.indexOf("\"", startIndex);
                        email = payload.substring(startIndex, endIndex);
                    } else {
                        searchStr = "\"sub\":\"";
                        startIndex = payload.indexOf(searchStr);
                        if (startIndex != -1) {
                            startIndex += searchStr.length();
                            int endIndex = payload.indexOf("\"", startIndex);
                            email = payload.substring(startIndex, endIndex);
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore parse errors and let it fall through to the 401 Unauthorized block
            }
        }

        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(userService.upgradeToPro(email));
    }
}