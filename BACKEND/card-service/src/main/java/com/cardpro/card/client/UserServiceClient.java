package com.cardpro.card.client;

import com.cardpro.card.dto.response.UserWatermarkResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Read-only bridge to user-service so the public card render can honor the
 * owner's profile preferences (currently: removeWatermark Pro perk). Mirrors
 * the existing inter-service pattern — the shared internal API key travels in
 * the {@code X-Internal-API-Key} header and user-service verifies it.
 */
@FeignClient(
        name = "user-service",
        url = "${USER_SERVICE_URL:http://localhost:8081}",
        path = "/api/v1/users/internal"
)
public interface UserServiceClient {

    @GetMapping("/watermark")
    UserWatermarkResponse getWatermark(
            @RequestParam("email") String email,
            @RequestHeader("X-Internal-API-Key") String apiKey
    );
}
