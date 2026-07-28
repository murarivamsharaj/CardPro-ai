package com.cardpro.auth.dto.response;

import com.cardpro.auth.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private Role role;
    private Integer leadCredits;
}
