package com.cardpro.auth.service;

import com.cardpro.auth.dto.request.LoginRequest;
import com.cardpro.auth.dto.request.RefreshTokenRequest;
import com.cardpro.auth.dto.request.RegisterRequest;
import com.cardpro.auth.dto.response.AuthResponse;
import com.cardpro.auth.dto.response.UserResponse;
import com.cardpro.auth.entity.RefreshToken;
import com.cardpro.auth.entity.User;
import com.cardpro.auth.enums.Role;
import com.cardpro.auth.exception.AccountDisabledException;
import com.cardpro.auth.exception.InvalidCredentialsException;
import com.cardpro.auth.exception.PasswordMismatchException;
import com.cardpro.auth.exception.RegistrationDisabledException;
import com.cardpro.auth.exception.TokenExpiredException;
import com.cardpro.auth.exception.UserAlreadyExistsException;
import com.cardpro.auth.repository.RefreshTokenRepository;
import com.cardpro.auth.repository.UserRepository;
import com.cardpro.auth.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RegistrationConfigService registrationConfigService;

    // ──────────────────────────────────────────────
    // Register
    // ──────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Global admin toggle: reject self-registration when disabled.
        if (!registrationConfigService.isRegistrationEnabled()) {
            throw new RegistrationDisabledException(
                "Public registration is currently disabled by an administrator"
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .leadCredits(25)
            .build();

        user = userRepository.save(user);
        log.info("User registered: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    // ──────────────────────────────────────────────
    // Login
    // ──────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isAccountActive()) {
            throw new AccountDisabledException(
                "This account has been disabled. Contact an administrator."
            );
        }

        log.info("User logged in: {}", user.getEmail());
        return generateAuthResponse(user);
    }

    // ──────────────────────────────────────────────
    // Refresh Token
    // ──────────────────────────────────────────────

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String tokenValue = request.getRefreshToken();

        // 1. Find the refresh token in the database
        RefreshToken storedToken = refreshTokenRepository.findByToken(tokenValue)
            .orElseThrow(() -> new TokenExpiredException("Refresh token not found or has been revoked"));

        // 2. Validate: account still active (soft-deleted users cannot refresh)
        if (!storedToken.getUser().isAccountActive()) {
            throw new AccountDisabledException(
                "This account has been disabled. Contact an administrator."
            );
        }

        // 3. Validate: not revoked, not expired
        if (storedToken.isRevoked()) {
            // Token reuse detected — revoke all tokens for this user (security measure)
            User user = storedToken.getUser();
            refreshTokenRepository.revokeAllUserTokens(user.getId());
            log.warn("Refresh token reuse detected for user: {}. All tokens revoked.", user.getEmail());
            throw new TokenExpiredException("Refresh token has been revoked. All tokens invalidated.");
        }

        if (storedToken.isExpired()) {
            throw new TokenExpiredException("Refresh token has expired. Please login again.");
        }

        // 4. Rotate: revoke the used token
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        // 5. Issue new tokens
        User user = storedToken.getUser();
        log.debug("Refresh token rotated for user: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    // ──────────────────────────────────────────────
    // Logout
    // ──────────────────────────────────────────────

    @Transactional
    public void logout(String userId) {
        UUID uuid = UUID.fromString(userId);
        int revoked = refreshTokenRepository.revokeAllUserTokens(uuid);
        log.info("User logged out: {} — revoked {} token(s)", userId, revoked);
    }

    // ──────────────────────────────────────────────
    // Password Change
    // ──────────────────────────────────────────────

    /**
     * Verifies the current password, hashes the new one, and persists it.
     * All existing refresh tokens are revoked so other sessions must log in
     * again with the new password.
     */
    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new PasswordMismatchException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        int revoked = refreshTokenRepository.revokeAllUserTokens(userId);
        log.info("Password changed for user: {} - revoked {} refresh token(s)", user.getEmail(), revoked);
    }

    // ──────────────────────────────────────────────
    // Admin: User Management
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUserRole(UUID userId, Role role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        user.setRole(role);
        user = userRepository.save(user);
        log.info("User {} role updated to {}", user.getEmail(), role);

        return mapToUserResponse(user);
    }

    /**
     * Soft-delete (disable) or re-enable an account. Disabled accounts cannot
     * log in, refresh tokens, or authenticate against JWT-protected endpoints.
     */
    @Transactional
    public UserResponse setUserEnabled(UUID userId, boolean enabled) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        user.setEnabled(enabled);
        user = userRepository.save(user);

        // A disabled account must not keep using its refresh tokens.
        if (!enabled) {
            refreshTokenRepository.revokeAllUserTokens(userId);
        }
        log.info("User {} {}", user.getEmail(), enabled ? "re-enabled" : "disabled");

        return mapToUserResponse(user);
    }

    // ──────────────────────────────────────────────
    // Internal: User Lookup
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    // ──────────────────────────────────────────────
    // Private Helpers
    // ──────────────────────────────────────────────

    private AuthResponse generateAuthResponse(User user) {
        UserPrincipal principal = new UserPrincipal(user);

        String accessToken = jwtService.generateAccessToken(principal);
        String refreshTokenValue = jwtService.generateRefreshToken(user.getId().toString());

        saveRefreshTokenEntity(user, refreshTokenValue);

        return AuthResponse.builder()
            .token(accessToken)
            .refreshToken(refreshTokenValue)
            .tokenType("Bearer")
            .expiresIn(jwtService.getAccessExpirationMs())
            .user(mapToUserResponse(user))
            .build();
    }

    private void saveRefreshTokenEntity(User user, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(token)
            .expiresAt(Instant.now().plusMillis(jwtService.getRefreshExpirationMs()))
            .build();
        refreshTokenRepository.save(refreshToken);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .role(user.getRole())
            .leadCredits(user.getLeadCredits())
            .enabled(user.getEnabled())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
