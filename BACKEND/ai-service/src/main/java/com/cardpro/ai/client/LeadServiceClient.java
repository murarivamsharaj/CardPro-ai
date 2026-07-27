package com.cardpro.ai.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "lead-service", path = "/api/v1/leads/internal")
public interface LeadServiceClient {

    @PostMapping("/{leadId}/followup")
    void updateFollowup(
        @PathVariable String leadId,
        @RequestBody String followup,
        @RequestHeader("X-Internal-API-Key") String apiKey
    );
}
