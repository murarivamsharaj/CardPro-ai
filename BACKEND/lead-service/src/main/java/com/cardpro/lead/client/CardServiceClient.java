package com.cardpro.lead.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "card-service", path = "/api/v1/cards/internal")
public interface CardServiceClient {

    @GetMapping("/{profileId}")
    Object getProfileById(
        @PathVariable UUID profileId,
        @RequestHeader("X-Internal-API-Key") String apiKey
    );
}
