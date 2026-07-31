package com.cardpro.orderservice.client;

import com.cardpro.orderservice.config.FeignClientConfig;
import com.cardpro.orderservice.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for inter-service communication with the product-service.
 * Used to validate that a product exists and to fetch its price when creating orders.
 * Automatically includes the X-Internal-Api-Key header via FeignClientConfig.
 */
@FeignClient(
        name = "product-service",
        path = "/api/products",
        configuration = FeignClientConfig.class
)
public interface ProductServiceClient {

    @GetMapping("/{id}")
    ProductResponse getProductById(@PathVariable("id") Long id);
}