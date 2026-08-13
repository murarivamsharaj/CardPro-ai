package com.cardpro.userservice.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code PUT /api/users/profile}. Only the profile-details
 * fields are editable here; the email is resolved from the caller's JWT so a
 * user can never rewrite another user's identity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    @Size(max = 120, message = "Display name must be at most 120 characters")
    private String displayName;

    @Size(max = 30, message = "Phone number must be at most 30 characters")
    private String phoneNumber;

    @Size(max = 120, message = "Job title must be at most 120 characters")
    private String jobTitle;
}
