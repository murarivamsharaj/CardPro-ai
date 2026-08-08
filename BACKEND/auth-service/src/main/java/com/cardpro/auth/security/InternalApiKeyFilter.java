package com.cardpro.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.internal.api-key}")
    private String internalApiKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // ONLY apply this filter to internal service routes. Skip everything else.
        return !request.getRequestURI().startsWith("/api/v1/auth/internal");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        log.info("InternalApiKeyFilter Executed for: {}", request.getRequestURI());

        String requestApiKey = request.getHeader("X-Internal-Api-Key");

        if (internalApiKey.equals(requestApiKey)) {
            log.info("Internal API Key validated successfully.");

            // Inform Spring Security that this request has the INTERNAL_SERVICE role
            UsernamePasswordAuthenticationToken internalAuth = new UsernamePasswordAuthenticationToken(
                    "internal-service-client",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE"))
            );
            SecurityContextHolder.getContext().setAuthentication(internalAuth);

            filterChain.doFilter(request, response);
            return;
        }

        log.warn("Invalid or Missing Internal API Key.");

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("""
            {
              "status":"error",
              "error":{
                "code":"FORBIDDEN",
                "message":"Invalid or missing Internal API Key"
              }
            }
            """);
    }
}