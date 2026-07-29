package com.cardpro.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Validates requests containing the {@code X-Internal-API-Key} header.
 *
 * <p>Used for inter-service communication where other microservices
 * (card-service, lead-service, etc.) call auth-service's internal
 * endpoints to validate users or retrieve user data.
 *
 * <p>If the API key matches, sets a privileged INTERNAL_SERVICE
 * authentication in the SecurityContext. If the key is present
 * but invalid, returns HTTP 401.
 */
@Slf4j
@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.internal.api-key}")
    private String internalApiKey;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestKey = request.getHeader("X-Internal-API-Key");

        if (!StringUtils.hasText(requestKey)) {
            // No internal key present — skip this filter
            filterChain.doFilter(request, response);
            return;
        }

        if (!internalApiKey.equals(requestKey)) {
            log.warn("Invalid internal API key from: {}", request.getRemoteAddr());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid internal API key");
            return;
        }

        // Authenticated internal service — set a privileged context
        Authentication auth = UsernamePasswordAuthenticationToken.authenticated(
            new InternalServicePrincipal(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    /**
     * Marker principal for internal service authentication.
     */
    private record InternalServicePrincipal() {
        @Override
        public String toString() {
            return "InternalService";
        }
    }
}
