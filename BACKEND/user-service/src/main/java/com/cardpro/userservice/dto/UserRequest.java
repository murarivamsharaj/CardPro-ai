package com.cardpro.userservice.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profileImage;
    private String role;
}