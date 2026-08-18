package com.cardpro.card.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey secretKey;

    public JwtAuthenticationFilter(@Value("${app.jwt.secret}") String secret) {
        if (secret == null || secret.isBlank()) {
            log.error("CRITICAL: app.jwt.secret is blank or missing in card-service!");
        }
        this.secretKey = Keys.hmacShaKeyFor((secret != null ? secret : "default_fallback_secret_key_change_me").getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.info("Incoming request path to card-service: {}", path);

        try {
            // 1. Check if Gateway forwarded X-User-Id
            String gatewayUserId = request.getHeader("X-User-Id");
            if (gatewayUserId != null && !gatewayUserId.isBlank()) {
                log.info("Found gateway X-User-Id header: {}", gatewayUserId);
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
                log.info("Successfully authenticated user via Gateway headers: {}", gatewayUserId);
            }
            else {
                log.warn("X-User-Id header missing! Attempting fallback token parse for path: {}", path);
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
                        log.info("Successfully authenticated user via fallback Bearer token: {}", userId);
                    }
                } else {
                    log.warn("No Authorization header and no X-User-Id header found for path: {}", path);
                }
            }
        } catch (Exception e) {
            log.error("FAILED to set user authentication due to exception: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}