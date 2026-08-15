package com.cardpro.userservice.controller;

import com.cardpro.userservice.dto.WatermarkResponse;
import com.cardpro.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal inter-service endpoints for user-service. NOT reachable through the
 * gateway (user-service is called directly), so the shared internal API key is
 * verified in the handler — the same {@code X-Internal-API-Key} contract the
 * other services use for their {@code /internal/**} routes.
 */
@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
@Tag(name = "Internal User", description = "Inter-service endpoints (internal API key required)")
public class InternalUserController {

    private final UserService userService;

    @Value("${app.internal.api-key}")
    private String internalApiKey;

    /**
     * Returns the card owner's removeWatermark preference so card-service can
     * decide whether to render the "Powered by CardPro" footer on a public
     * card. Fails closed: unknown email → watermark stays visible.
     */
    @GetMapping("/watermark")
    @Operation(summary = "Card owner's removeWatermark preference (internal)")
    public ResponseEntity<WatermarkResponse> getWatermark(
            @RequestParam("email") String email,
            @RequestHeader(value = "X-Internal-API-Key", required = false) String apiKey) {
        if (apiKey == null || !apiKey.equals(internalApiKey)) {
            return ResponseEntity.status(403).build();
        }
        boolean removeWatermark = userService.getWatermarkPreference(email);
        return ResponseEntity.ok(WatermarkResponse.builder()
                .removeWatermark(removeWatermark)
                .build());
    }
}
