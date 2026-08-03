package com.cardpro.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    @Value("${internal.api.key:my-secret-internal-key}")
    private String internalApiKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Completely bypass internal API key check for public authentication and documentation routes
        return path.startsWith("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/register") ||
                path.startsWith("/api/v1/auth/refresh") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestApiKey = request.getHeader("X-Internal-Api-Key");

        if (internalApiKey.equals(requestApiKey)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":\"error\",\"error\":{\"code\":\"FORBIDDEN\"," +
                            "\"message\":\"Invalid or missing Internal API Key\"}}"
            );
        }
    }
}