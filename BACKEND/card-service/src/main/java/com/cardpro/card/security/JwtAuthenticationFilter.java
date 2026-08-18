package com.cardpro.card.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey secretKey;

    public JwtAuthenticationFilter(@Value("${app.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. First, check if the Gateway already authenticated and forwarded user identity headers
            String gatewayUserId = request.getHeader("X-User-Id");
            if (gatewayUserId != null && !gatewayUserId.isBlank() && SecurityContextHolder.getContext().getAuthentication() == null) {
                String email = request.getHeader("X-User-Email");
                String rolesHeader = request.getHeader("X-User-Roles");

                List<SimpleGrantedAuthority> authorities;
                if (rolesHeader != null && !rolesHeader.isBlank()) {
                    authorities = Arrays.stream(rolesHeader.split(","))
                            .map(String::trim)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                } else {
                    authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        new CardUserPrincipal(gatewayUserId, email), null, authorities
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // 2. Fallback: Parse raw Authorization header if hit directly or gateway headers are absent
            else if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String header = request.getHeader("Authorization");
                if (header != null && header.startsWith("Bearer ")) {
                    String token = header.substring(7);

                    Claims claims = Jwts.parser()
                            .verifyWith(secretKey)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

                    String userId = claims.getSubject();
                    String email = claims.get("email", String.class);

                    @SuppressWarnings("unchecked")
                    List<String> roles = claims.get("roles", List.class);

                    if (userId != null && roles != null) {
                        List<SimpleGrantedAuthority> authorities = roles.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                new CardUserPrincipal(userId, email), null, authorities
                        );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }
}