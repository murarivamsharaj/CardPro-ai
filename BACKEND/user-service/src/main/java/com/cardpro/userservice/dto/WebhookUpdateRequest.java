package com.cardpro.userservice.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code PUT /api/users/webhook}. The webhook URL is where
 * future CRM lead-forwarding integrations can POST new leads. An empty string
 * clears the webhook (saved as null).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookUpdateRequest {

    @Size(max = 500, message = "Webhook URL must be at most 500 characters")
    private String webhookUrl;
}
