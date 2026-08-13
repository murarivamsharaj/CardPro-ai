package com.cardpro.userservice.controller;

import com.cardpro.userservice.dto.NotificationPreferenceRequest;
import com.cardpro.userservice.dto.ProfileUpdateRequest;
import com.cardpro.userservice.dto.UserResponse;
import com.cardpro.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Authenticated profile endpoints for the Settings page.
 *
 * <p>The caller is identified by the JWT's <b>email</b> claim
 * ({@code principal.getName()}), so a user can only read/update their own
 * profile — tenant isolation is enforced at the server, never trusted to the
 * request body.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Own-profile details and notification preferences (authenticated)")
public class ProfileController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile")
    public ResponseEntity<UserResponse> getMyProfile(Principal principal) {
        return ResponseEntity.ok(userService.getProfileByEmail(principal.getName()));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update display name, phone number, and job title")
    public ResponseEntity<UserResponse> updateProfile(
            Principal principal,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(userService.updateProfile(principal.getName(), request));
    }

    @PutMapping("/notifications")
    @Operation(summary = "Toggle email notifications (publishes a RabbitMQ event)")
    public ResponseEntity<UserResponse> updateNotificationPreference(
            Principal principal,
            @Valid @RequestBody NotificationPreferenceRequest request) {
        return ResponseEntity.ok(
                userService.updateNotificationPreference(principal.getName(), request)
        );
    }
}
