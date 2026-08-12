package com.cardpro.lead.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class SubmitLeadRequest {

    // @NotBlank only applies to CharSequence — on a UUID it makes Hibernate
    // Validator throw UnexpectedTypeException for EVERY submission, which then
    // surfaces to the client as a 403 (the /error dispatch is secured). Use
    // @NotNull instead so a valid UUID passes and a missing one fails cleanly.
    @NotNull(message = "Profile ID is required")
    private UUID profileId;

    @NotBlank(message = "Visitor name is required")
    private String visitorName;

    @Email(message = "Visitor email must be a valid email address")
    private String visitorEmail;

    @Size(max = 20, message = "Visitor phone must be at most 20 characters")
    private String visitorPhone;

    private String message;
}
