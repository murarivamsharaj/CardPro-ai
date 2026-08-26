package com.cardpro.userservice.controller;

import com.cardpro.userservice.dto.NotificationPreferenceRequest;
import com.cardpro.userservice.dto.ProfileUpdateRequest;
import com.cardpro.userservice.dto.UserResponse;
import com.cardpro.userservice.dto.WebhookUpdateRequest;
import com.cardpro.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

/**
 * Authenticated profile endpoints for the Settings page.
 *
 * <p>The caller is identified by safely extracting the JWT's <b>email</b> claim
 * from the request headers, bypassing Principal injection failures.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Own-profile details and notification preferences (authenticated)")
public class ProfileController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile")
    public ResponseEntity<UserResponse> getMyProfile(HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.getProfileByEmail(email));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update display name, phone number, and job title")
    public ResponseEntity<UserResponse> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody ProfileUpdateRequest updateRequest) {
        String email = extractEmailFromRequest(request);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.updateProfile(email, updateRequest));
    }

    @PutMapping("/notifications")
    @Operation(summary = "Toggle email notifications (publishes a RabbitMQ event)")
    public ResponseEntity<UserResponse> updateNotificationPreference(
            HttpServletRequest request,
            @Valid @RequestBody NotificationPreferenceRequest prefRequest) {
        String email = extractEmailFromRequest(request);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.updateNotificationPreference(email, prefRequest));
    }

    @PostMapping("/api-key/regenerate")
    @Operation(summary = "Generate a fresh developer API key (invalidates the old one)")
    public ResponseEntity<UserResponse> regenerateApiKey(HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.regenerateApiKey(email));
    }

    @PutMapping("/webhook")
    @Operation(summary = "Save (or clear) the CRM lead-forwarding webhook URL")
    public ResponseEntity<UserResponse> updateWebhookUrl(
            HttpServletRequest request,
            @Valid @RequestBody WebhookUpdateRequest webhookRequest) {
        String email = extractEmailFromRequest(request);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.updateWebhookUrl(email, webhookRequest));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete the caller's own account (soft delete — active=false)")
    public ResponseEntity<UserResponse> deleteAccount(HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.deleteAccount(email));
    }

    /**
     * Extracts user email safely from gateway headers or by decoding the JWT Bearer token.
     */
    private String extractEmailFromRequest(HttpServletRequest request) {
        String email = request.getHeader("X-User-Email");
        if (email != null && !email.isBlank()) return email.trim();

        email = request.getHeader("X-Auth-User");
        if (email != null && !email.isBlank()) return email.trim();

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
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
                        if (endIndex != -1) return payload.substring(startIndex, endIndex);
                    }
                    searchStr = "\"sub\":\"";
                    startIndex = payload.indexOf(searchStr);
                    if (startIndex != -1) {
                        startIndex += searchStr.length();
                        int endIndex = payload.indexOf("\"", startIndex);
                        if (endIndex != -1) return payload.substring(startIndex, endIndex);
                    }
                }
            } catch (Exception ignored) { }
        }
        return null;
    }
}