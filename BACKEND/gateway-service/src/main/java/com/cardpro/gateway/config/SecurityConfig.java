//package com.cardpro.gateway.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//
//@Configuration
//@EnableWebFluxSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
//        http
//                .csrf(ServerHttpSecurity.CsrfSpec::disable)
//                .cors(ServerHttpSecurity.CorsSpec::disable) // Or let CorsWebFilter handle it
//                .authorizeExchange(exchanges -> exchanges
//                        // --> ADD THIS LINE RIGHT HERE: Allow all OPTIONS preflight checks <--
//                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//
//                        // Public auth routes
//                        .pathMatchers("/api/v1/auth/**").permitAll()
//
//                        // All other routes require authentication
//                        .anyExchange().authenticated()
//                );
//
//        return http.build();
//    }
//}