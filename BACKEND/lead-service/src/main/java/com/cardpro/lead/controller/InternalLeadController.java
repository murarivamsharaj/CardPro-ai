package com.cardpro.lead.controller;

import com.cardpro.lead.service.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
