package com.cardpro.lead.controller;

import com.cardpro.lead.service.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leads/internal")
@RequiredArgsConstructor
public class InternalLeadController {

    private final LeadService leadService;

    @PostMapping("/credits/deduct")
    public ResponseEntity<Void> deductCredits(@RequestParam String userId) {
        leadService.deductCredit(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Read-only lead count for a single card profile, consumed by card-service
     * so the analytics dashboard can report real lead volume and conversion
     * rates. Internal API only (permitAll at the gateway-facing security layer
     * like the other {@code /internal/**} routes).
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countLeads(@RequestParam UUID profileId) {
        return ResponseEntity.ok(leadService.countLeadsForProfile(profileId));
    }
}
