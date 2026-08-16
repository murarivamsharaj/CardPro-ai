package com.cardpro.card.config;

import com.cardpro.card.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Documentation + health probes
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/**"
                        ).permitAll()

                        // Internal routes — MUST be declared before the generic card
                        // matchers below (e.g. GET /api/v1/cards/**), otherwise
                        // /api/v1/cards/internal/{id} is captured by the authenticated
                        // rule first and inter-service Feign calls (which carry no JWT,
                        // only X-Internal-API-Key) are rejected 403.
                        .requestMatchers("/api/v1/cards/internal/**").permitAll()

                        // Card owner endpoints — MUST be checked before the public {slug}
                        // matcher below, otherwise "me" would match {slug} and become public.
                        .requestMatchers(HttpMethod.GET, "/api/v1/cards/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/cards/me").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/cards/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/cards").authenticated()

                        // Public slug lookup — unauthenticated visitors viewing a card
                        // at /c/:slug (GET /api/v1/cards/{slug}) or through the explicit
                        // public routes /api/v1/cards/public/{slug} and /api/v1/cards/slug/{slug}
                        .requestMatchers(HttpMethod.GET, "/api/v1/cards/{slug}").permitAll()
                        .requestMatchers("/api/v1/cards/public/**", "/api/v1/cards/slug/**").permitAll()

                        // Public analytics event ingestion from the card viewer
                        // (PAGE_VIEW, SOCIAL_CLICK, BUTTON_CLICK, VCF_DOWNLOAD).
                        // Every other /api/v1/analytics/** route (summary, overview,
                        // admin metrics) stays JWT-protected.
                        .requestMatchers(HttpMethod.POST, "/api/v1/analytics/events").permitAll()

                        // Uploaded card avatars must render for ANY visitor of the
                        // public card viewer, so serving them is unauthenticated.
                        // Uploading (POST) stays JWT-protected.
                        .requestMatchers(HttpMethod.GET, "/api/v1/files/view/**").permitAll()

                        // All private/management endpoints require a valid JWT
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
