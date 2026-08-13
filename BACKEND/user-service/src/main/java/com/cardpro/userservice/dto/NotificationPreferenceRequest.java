package com.cardpro.userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code PUT /api/users/notifications}. The flag is persisted
 * and broadcast as a RabbitMQ event so the lead-notification pipeline can
 * honor per-user preferences.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRequest {

    @NotNull(message = "enabled is required")
    private Boolean enabled;
}
