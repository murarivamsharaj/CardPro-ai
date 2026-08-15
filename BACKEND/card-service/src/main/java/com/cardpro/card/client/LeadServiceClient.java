package com.cardpro.card.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/**
 * Read-only bridge to lead-service so the analytics dashboard can include
 * real lead counts (and thus conversion rates) without duplicating the lead
 * data in card-service's database. Mirrors the existing inter-service pattern
 * (lead-service already calls card-service over Feign with the internal key).
 */
@FeignClient(name = "lead-service", path = "/api/v1/leads/internal")
public interface LeadServiceClient {

    @GetMapping("/count")
    long countLeads(
            @RequestParam("profileId") UUID profileId,
            @RequestHeader("X-Internal-API-Key") String apiKey
    );
}
