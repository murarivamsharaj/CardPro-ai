package com.cardpro.auth.service;

import com.cardpro.auth.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages JWT access and refresh token creation, validation, and parsing.
 * Uses HMAC-SHA256 with a configurable secret key.
 */
@Slf4j
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long accessExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    // ──────────────────────────────────────────────
    // Token Generation
    // ──────────────────────────────────────────────

    /**
     * Generates an access JWT for the given user principal.
     */
    public String generateAccessToken(UserPrincipal principal) {
        return generateAccessToken(
            principal.getId().toString(),
            principal.getEmail(),
            List.of(principal.getRole().name())
        );
    }

    /**
     * Generates an access JWT with explicit claims.
     */
    public String generateAccessToken(String userId, String email, List<String> roles) {
        Date now = new Date();
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(userId)
            .claim("email", email)
            .claim("roles", roles)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + accessExpirationMs))
            .signWith(secretKey)
            .compact();
    }

    /**
     * Generates a long-lived refresh token for the given user.
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(userId)
            .claim("type", "refresh")
            .issuedAt(now)
            .expiration(new Date(now.getTime() + refreshExpirationMs))
            .signWith(secretKey)
            .compact();
    }

    // ──────────────────────────────────────────────
    // Token Extraction
    // ──────────────────────────────────────────────

    /**
     * Extracts the user ID (subject) from a JWT.
     */
    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extracts the email claim from a JWT.
     */
    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    /**
     * Extracts the roles claim from a JWT.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        List<String> roles = extractClaims(token).get("roles", List.class);
        return roles != null
            ? roles.stream()
                .map(Object::toString)
                .collect(Collectors.toList())
            : List.of();
    }

    /**
     * Extracts the JWT ID (jti) for blacklist lookup.
     */
    public String extractTokenId(String token) {
        return extractClaims(token).getId();
    }

    /**
     * Extracts the expiration date from a JWT.
     */
    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }

    // ──────────────────────────────────────────────
    // Token Validation
    // ──────────────────────────────────────────────

    /**
     * Validates a JWT and returns the claims payload.
     * Throws on invalid signature, expired token, or malformed token.
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Returns true if the token is structurally valid (correct signature, not expired).
     */
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return !isTokenExpired(token);
        } catch (SecurityException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Returns true if the JWT has expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Returns the configured access token expiration in milliseconds.
     */
    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }

    /**
     * Returns the configured refresh token expiration in milliseconds.
     */
    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }
}
