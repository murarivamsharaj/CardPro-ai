package com.cardpro.lead.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "card-service", path = "/api/v1/cards/internal")
public interface CardServiceClient {

    @GetMapping("/{profileId}")
    CardProfileResponse getProfileById(
        @PathVariable UUID profileId,
        @RequestHeader("X-Internal-API-Key") String apiKey
    );

    /**
     * Fetches the card(s) owned by the logged-in user so lead-service can scope
     * lead queries. The {@code X-User-Id} header (injected by the gateway from
     * the JWT on the original request) is forwarded to card-service, which
     * resolves the owning card profile from it.
     */
    @GetMapping("/me")
    CardProfileResponse getMyCard(
        @RequestHeader("X-User-Id") String userId,
        @RequestHeader("X-Internal-API-Key") String apiKey
    );
}
