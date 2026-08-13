package com.cardpro.auth.entity;

import com.cardpro.auth.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "id")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    @EqualsAndHashCode.Include
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "lead_credits", nullable = false)
    @Builder.Default
    private Integer leadCredits = 25;

    /**
     * Soft-delete / disable flag. When false the account cannot log in,
     * refresh tokens are rejected, and JWT-authenticated calls are refused.
     *
     * <p>Deliberately nullable so ddl-auto:update can add the column to an
     * existing users table without a NOT NULL migration; NULL is treated as
     * enabled so pre-existing accounts are never locked out.
     */
    @Column(name = "enabled")
    @org.hibernate.annotations.ColumnDefault("true")
    @Builder.Default
    private Boolean enabled = true;

    /** Returns true unless the account was explicitly disabled. */
    public boolean isAccountActive() {
        return enabled == null || enabled;
    }

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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
}
