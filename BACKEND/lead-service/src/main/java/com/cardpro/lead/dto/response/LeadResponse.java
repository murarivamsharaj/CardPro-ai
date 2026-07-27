package com.cardpro.lead.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class LeadResponse {
    private UUID id;
    private UUID profileId;
    private String visitorName;
    private String visitorPhone;
    private String aiFollowup;
    private LocalDateTime capturedAt;
}
