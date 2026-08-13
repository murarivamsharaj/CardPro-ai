package com.cardpro.auth.dto.request;

import com.cardpro.auth.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body for the admin role-change endpoint (promote/demote).
 */
@Data
public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;
}
