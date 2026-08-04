package com.cardpro.card.config;

import com.cardpro.card.security.InternalApiKeyFilter;
import com.cardpro.card.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
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
    private final InternalApiKeyFilter internalApiKeyFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 👇 CRUCIAL ADDITION: Allow CORS preflight requests from the browser
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Explicitly match GET for public slug viewing
                        .requestMatchers(HttpMethod.GET, "/api/v1/cards/{slug}").permitAll()

                        // Explicitly allow POST/GET for card management for authenticated users
                        .requestMatchers(HttpMethod.POST, "/api/v1/cards").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/cards/**").authenticated()

                        // Internal routes
                        .requestMatchers("/api/v1/cards/internal/**").permitAll()

                        // Admin routes
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // All other routes require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(internalApiKeyFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}