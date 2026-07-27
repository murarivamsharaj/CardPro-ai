package com.cardpro.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${app.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String getUserId(String token) {
        return validateToken(token).getSubject();
    }

    public String getEmail(String token) {
        return validateToken(token).get("email", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        return validateToken(token).get("roles", List.class);
    }

    public boolean isTokenExpired(String token) {
        try {
            validateToken(token);
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
