package com.cardpro.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateToken(String userId, String email, List<String> roles) {
        Date now = new Date();
        return Jwts.builder()
            .subject(userId)
            .claim("email", email)
            .claim("roles", roles)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + expirationMs))
            .signWith(secretKey)
            .compact();
    }

    public String generateRefreshToken(String userId) {
        Date now = new Date();
        return Jwts.builder()
            .subject(userId)
            .id(UUID.randomUUID().toString())
            .issuedAt(now)
            .expiration(new Date(now.getTime() + refreshExpirationMs))
            .signWith(secretKey)
            .compact();
    }

    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
