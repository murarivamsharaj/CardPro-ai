package com.cardpro.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body for the admin enable/disable (soft-delete) endpoint.
 */
@Data
public class UpdateUserStatusRequest {

    @NotNull(message = "enabled is required")
    private Boolean enabled;
}
