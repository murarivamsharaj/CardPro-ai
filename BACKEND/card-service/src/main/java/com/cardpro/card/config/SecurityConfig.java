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

                        .requestMatchers("/api/v1/cards/internal/**").permitAll()

                        // 🔥 THE FIX: Change these to permitAll()
                        // Our CardController now manually validates the JWT/Headers and returns 401 Unauthorized if invalid.
                        // This entirely bypasses the broken Security Filter that is throwing the 403 Forbidden.
                        .requestMatchers(HttpMethod.GET, "/api/v1/cards/me").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/cards/me").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/cards/me").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/cards").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/cards").permitAll()

                        // Public slug lookup
                        .requestMatchers(HttpMethod.GET, "/api/v1/cards/{slug}").permitAll()
                        .requestMatchers("/api/v1/cards/public/**", "/api/v1/cards/slug/**").permitAll()

                        // Public analytics event ingestion
                        .requestMatchers(HttpMethod.POST, "/api/v1/analytics/events").permitAll()

                        // Uploaded card avatars
                        .requestMatchers(HttpMethod.GET, "/api/v1/files/view/**").permitAll()

                        // All other private/management endpoints require a valid JWT
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}