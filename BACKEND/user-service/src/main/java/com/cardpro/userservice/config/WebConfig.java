package com.cardpro.userservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS for direct browser calls to user-service.
 *
 * <p>user-service is intentionally not routed through the gateway (gateway
 * routing is stable/off-limits), so the frontend calls it directly on its
 * host port. These origins cover Vite dev (5173/5174) and the Dockerized
 * nginx frontend (3000).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/api/**")
//                .allowedOrigins(
//                        "http://localhost:5173",
//                        "http://localhost:5174",
//                        "http://localhost:3000",
//                        "http://127.0.0.1:5173",
//                        "http://127.0.0.1:5174",
//                        "http://127.0.0.1:3000"
//                )
//                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//                .allowCredentials(true);
    }
}
