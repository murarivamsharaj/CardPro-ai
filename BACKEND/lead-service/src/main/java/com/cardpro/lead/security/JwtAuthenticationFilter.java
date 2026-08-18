package com.cardpro.lead.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Read Gateway-injected headers
            String gatewayUserId = request.getHeader("X-User-Id");
            if (gatewayUserId != null && !gatewayUserId.isBlank()) {
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
                        new LeadUserPrincipal(gatewayUserId, email), null, authorities
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("Lead-service Auth Filter Error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    // Inner Principal class to store userId and email safely
    public static class LeadUserPrincipal implements Principal {
        private final String userId;
        private final String email;

        public LeadUserPrincipal(String userId, String email) {
            this.userId = userId;
            this.email = email;
        }

        @Override
        public String getName() {
            return userId;
        }

        public String getEmail() {
            return email;
        }
    }
}