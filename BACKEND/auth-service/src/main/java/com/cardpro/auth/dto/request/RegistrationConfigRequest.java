package com.cardpro.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request/response body for the global public-registration flag.
 */
@Data
public class RegistrationConfigRequest {

    @NotNull(message = "enabled is required")
    private Boolean enabled;
}
