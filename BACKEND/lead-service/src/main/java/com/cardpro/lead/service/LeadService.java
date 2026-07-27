package com.cardpro.lead.service;

import com.cardpro.lead.dto.request.SubmitLeadRequest;
import com.cardpro.lead.dto.response.LeadResponse;
import com.cardpro.lead.entity.Lead;
import com.cardpro.lead.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadEventPublisher leadEventPublisher;

    public LeadResponse submitLead(SubmitLeadRequest request) {
        Lead lead = Lead.builder()
            .profileId(request.getProfileId())
            .visitorName(request.getVisitorName())
            .visitorPhone(request.getVisitorPhone())
            .build();

        lead = leadRepository.save(lead);

        // Publish async event for AI follow-up generation
        leadEventPublisher.publishLeadCreated(lead);

        return mapToResponse(lead);
    }

    public Page<LeadResponse> getLeadsByUserId(String userId, int page, int size) {
        // In production, resolve profileId from userId via auth-service
        return Page.empty();
    }

    public String getFollowup(String leadId) {
        Lead lead = leadRepository.findById(UUID.fromString(leadId))
            .orElseThrow(() -> new RuntimeException("Lead not found"));
        return lead.getAiFollowup() != null ? lead.getAiFollowup() : "No follow-up generated yet.";
    }

    public void deductCredit(String userId) {
        // Integrate with auth-service for credit deduction
    }

    private LeadResponse mapToResponse(Lead lead) {
        return LeadResponse.builder()
            .id(lead.getId())
            .profileId(lead.getProfileId())
            .visitorName(lead.getVisitorName())
            .visitorPhone(lead.getVisitorPhone())
            .aiFollowup(lead.getAiFollowup())
            .capturedAt(lead.getCapturedAt())
            .build();
    }
}
