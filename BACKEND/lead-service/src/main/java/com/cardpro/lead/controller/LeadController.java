package com.cardpro.lead.controller;

import com.cardpro.lead.dto.request.SubmitLeadRequest;
import com.cardpro.lead.dto.response.LeadResponse;
import com.cardpro.lead.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    public ResponseEntity<LeadResponse> submitLead(@Valid @RequestBody SubmitLeadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leadService.submitLead(request));
    }

    @GetMapping
    public ResponseEntity<Page<LeadResponse>> getLeads(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "") String search) {
        return ResponseEntity.ok(leadService.getLeadsByUserId(userId, page, size, search));
    }

    @GetMapping("/{id}/followup")
    public ResponseEntity<String> getFollowup(@PathVariable String id) {
        return ResponseEntity.ok(leadService.getFollowup(id));
    }
}
