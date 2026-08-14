package com.cardpro.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    /** Display name shown on the user's profile/settings (nullable). */
    private String displayName;

    /** Job title shown on the user's profile/settings (nullable). */
    private String jobTitle;

    private String phoneNumber;

    private String profileImage;

    @Column(nullable = false)
    private String role; // e.g., "ROLE_USER" or "ROLE_ADMIN"

    @Builder.Default
    private boolean active = true;

    /** Whether the user has paid for the CardPro Pro subscription. */
    @Builder.Default
    private boolean isPro = false;

    /** Master preference consumed by the lead-notification pipeline. */
    @Builder.Default
    private Boolean emailNotificationsEnabled = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}