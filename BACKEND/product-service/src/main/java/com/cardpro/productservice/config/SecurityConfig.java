//package com.cardpro.productservice.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                // 1. Disable CORS here because gateway-service handles CORS globally
//                .cors(AbstractHttpConfigurer::disable)
//
//                // 2. Disable CSRF for stateless REST APIs
//                .csrf(AbstractHttpConfigurer::disable)
//
//                // 3. Stateless sessions
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                // 4. Authorize requests forwarded from the API Gateway
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow preflight
//                        .requestMatchers("/api/products/**").permitAll()        // Allow Gateway traffic
//                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
//                        .requestMatchers("/actuator/**").permitAll()
//                        .anyRequest().permitAll()
//                );
//
//        return http.build();
//    }
//}