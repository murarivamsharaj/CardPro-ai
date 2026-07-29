package com.cardpro.auth.controller;

import com.cardpro.auth.dto.request.LoginRequest;
import com.cardpro.auth.dto.request.RefreshTokenRequest;
import com.cardpro.auth.dto.request.RegisterRequest;
import com.cardpro.auth.dto.response.AuthResponse;
import com.cardpro.auth.security.UserPrincipal;
import com.cardpro.auth.service.AuthService;
import com.cardpro.auth.service.JwtService;
import com.cardpro.auth.service.TokenBlacklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller exposing register, login, refresh, and logout endpoints.
 *
 * <p><b>Security:</b>
 * <ul>
 *   <li>{@code POST /register} — PUBLIC (no auth required)</li>
 *   <li>{@code POST /login} — PUBLIC (no auth required)</li>
 *   <li>{@code POST /refresh} — PUBLIC (uses old refresh token, not JWT)</li>
 *   <li>{@code POST /logout} — AUTHENTICATED (requires valid JWT)</li>
 * </ul>
 *
 * <p>All endpoints produce {@code application/json} responses.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Register a new user account.
     *
     * @param request {@link RegisterRequest} with email and password
     * @return 201 Created with {@link AuthResponse} containing JWT tokens
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("Registration request for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate an existing user.
     *
     * @param request {@link LoginRequest} with email and password
     * @return 200 OK with {@link AuthResponse} containing JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Issue a new access token using a valid refresh token (token rotation).
     * The old refresh token is revoked and a new one is issued.
     *
     * @param request {@link RefreshTokenRequest} with the refresh token value
     * @return 200 OK with new {@link AuthResponse}
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Refresh token request received");
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Invalidate all refresh tokens for the authenticated user
     * and blacklist the current access token so it's immediately unusable.
     *
     * @param authentication Spring Security {@link Authentication} containing user principal + JWT credentials
     * @return 204 No Content on successful logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            String userId = principal.getId().toString();

            // 1. Revoke all refresh tokens in the database
            authService.logout(userId);

            // 2. Blacklist the current access token so it cannot be used again
            String token = (String) authentication.getCredentials();
            if (token != null) {
                String tokenId = jwtService.extractTokenId(token);
                tokenBlacklistService.blacklistToken(tokenId, jwtService.getAccessExpirationMs());
            }

            log.info("Logout successful for user: {}", userId);
        }
        return ResponseEntity.noContent().build();
    }
}
