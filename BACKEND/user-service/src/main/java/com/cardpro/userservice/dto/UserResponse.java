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
    private boolean pro;
    /** Pro perk: hides the "Powered by CardPro" watermark on public cards. */
    private boolean removeWatermark;
    /** Auto-generated developer API key (regenerable from Settings). */
    private String apiKey;
    /** Webhook URL for future CRM lead-forwarding integrations. */
    private String webhookUrl;
    private Boolean emailNotificationsEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
