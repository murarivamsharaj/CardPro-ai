package com.cardpro.card.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "card_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "template_id", nullable = false, length = 50)
    @Builder.Default
    private String templateId = "basic";

    @Type(JsonBinaryType.class)
    @Column(name = "profile_data", columnDefinition = "jsonb", nullable = false)
    private String profileData;

    @Column(name = "ai_avatar_url", length = 500)
    private String aiAvatarUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
