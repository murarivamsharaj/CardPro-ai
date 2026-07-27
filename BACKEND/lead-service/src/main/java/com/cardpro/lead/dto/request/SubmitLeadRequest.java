package com.cardpro.lead.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class SubmitLeadRequest {

    @NotBlank(message = "Profile ID is required")
    private UUID profileId;

    @NotBlank(message = "Visitor name is required")
    private String visitorName;

    @NotBlank(message = "Visitor phone is required")
    private String visitorPhone;
}
