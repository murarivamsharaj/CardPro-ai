    package com.cardpro.userservice.dto;

    import lombok.*;
    import java.time.LocalDateTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class UserResponse {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String displayName;
        private String jobTitle;
        private String phoneNumber;
        private String profileImage;
        private String role;
        private boolean active;
        private Boolean emailNotificationsEnabled;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }