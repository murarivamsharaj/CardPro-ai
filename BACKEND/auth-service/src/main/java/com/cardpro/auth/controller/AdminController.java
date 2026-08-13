package com.cardpro.auth.controller;

import com.cardpro.auth.dto.request.RegistrationConfigRequest;
import com.cardpro.auth.dto.request.UpdateUserRoleRequest;
import com.cardpro.auth.dto.request.UpdateUserStatusRequest;
import com.cardpro.auth.dto.response.UserResponse;
import com.cardpro.auth.service.AuthService;
import com.cardpro.auth.service.RegistrationConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin command center endpoints. Every route in this controller is guarded by
 * {@code hasRole("ADMIN")} in {@link com.cardpro.auth.config.SecurityConfig}.
 *
 * <ul>
 *   <li>{@code GET /users} — full user list (including disabled accounts)</li>
 *   <li>{@code PATCH /users/{id}/role} — promote / demote</li>
 *   <li>{@code PATCH /users/{id}/status} — enable / disable (soft delete)</li>
 *   <li>{@code DELETE /users/{id}} — soft delete alias</li>
 *   <li>{@code GET/PUT /config/registration} — global self-registration flag</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only user management and platform configuration")
public class AdminController {

    private final AuthService authService;
    private final RegistrationConfigService registrationConfigService;

    @GetMapping("/users")
    @Operation(summary = "List all users (Admin only)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Promote or demote a user (Admin only)")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(authService.updateUserRole(id, request.getRole()));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Enable or disable a user account (Admin only)")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            Authentication authentication) {
        ensureNotSelf(id, authentication, "disable or re-enable");
        return ResponseEntity.ok(authService.setUserEnabled(id, request.getEnabled()));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Soft-delete a user account (Admin only)")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id,
            Authentication authentication) {
        ensureNotSelf(id, authentication, "delete");
        authService.setUserEnabled(id, false);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/config/registration")
    @Operation(summary = "Get the global public-registration flag (Admin only)")
    public ResponseEntity<Map<String, Boolean>> getRegistrationConfig() {
        return ResponseEntity.ok(Map.of(
            "enabled", registrationConfigService.isRegistrationEnabled()
        ));
    }

    @PutMapping("/config/registration")
    @Operation(summary = "Enable or disable global public registration (Admin only)")
    public ResponseEntity<Map<String, Boolean>> setRegistrationConfig(
            @Valid @RequestBody RegistrationConfigRequest request) {
        registrationConfigService.setRegistrationEnabled(Boolean.TRUE.equals(request.getEnabled()));
        log.info("Global registration {} by admin", request.getEnabled() ? "enabled" : "disabled");
        return ResponseEntity.ok(Map.of(
            "enabled", registrationConfigService.isRegistrationEnabled()
        ));
    }

    /**
     * Admins must not be able to soft-delete or lock out their own account.
     */
    private void ensureNotSelf(UUID id, Authentication authentication, String action) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return;
        }
        String callerId = extractPrincipalId(authentication);
        if (callerId != null && callerId.equals(id.toString())) {
            throw new IllegalArgumentException("You cannot " + action + " your own account");
        }
    }

    private String extractPrincipalId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.cardpro.auth.security.UserPrincipal userPrincipal) {
            return userPrincipal.getId().toString();
        }
        return principal != null ? principal.toString() : null;
    }
}
