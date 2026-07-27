package com.cardpro.lead.service;

import com.cardpro.lead.entity.Lead;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadEventPublisher {

    private final ObjectMapper objectMapper;

    public void publishLeadCreated(Lead lead) {
        try {
            String message = objectMapper.writeValueAsString(lead);
            // Publish to Redis Stream "lead:created"
            log.info("Published lead:created event for lead: {}", lead.getId());
        } catch (Exception e) {
            log.error("Failed to publish lead:created event", e);
        }
    }
}
