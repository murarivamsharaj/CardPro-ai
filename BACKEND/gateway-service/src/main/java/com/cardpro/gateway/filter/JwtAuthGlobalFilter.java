package com.cardpro.gateway.filter;

import com.cardpro.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public JwtAuthGlobalFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. COMPLETELY BYPASS JWT VALIDATION FOR PUBLIC AUTH ROUTES
        if (path.startsWith("/api/v1/auth/")) {
            return chain.filter(exchange);
        }

        // 2. Token validation for other protected routes
        if (!request.getHeaders().containsKey("Authorization")) {
            return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Invalid Authorization Header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // 3. Validate the JWT and extract the user identity so downstream services
        //    (e.g. analytics, leads) can resolve the caller via X-User-Id.
        Claims claims;
        try {
            claims = jwtUtil.validateToken(token);
        } catch (Exception e) {
            return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
        }

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", claims.getSubject())
                .header("X-User-Email", claims.get("email", String.class))
                .header("X-User-Roles", String.join(",", rolesFrom(claims)))
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @SuppressWarnings("unchecked")
    private List<String> rolesFrom(Claims claims) {
        List<String> roles = claims.get("roles", List.class);
        return roles != null ? roles : List.of();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}