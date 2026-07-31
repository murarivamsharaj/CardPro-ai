package com.cardpro.orderservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenFeign configuration that automatically attaches the X-Internal-Api-Key
 * header to all outgoing inter-service requests.
 */
@Configuration
public class FeignClientConfig {

    @Value("${app.internal.api-key:${INTERNAL_API_KEY:cardpro-internal-secret-key}}")
    private String internalApiKey;

    @Bean
    public RequestInterceptor internalApiKeyInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.header("X-Internal-Api-Key", internalApiKey);
            }
        };
    }
}