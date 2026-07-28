package com.cardpro.auth.repository;

import com.cardpro.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing refresh token persistence.
 * Supports token lookup, user-based queries, and bulk revocation.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Find a refresh token by its JWT string value.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all non-revoked refresh tokens for a user.
     */
    Optional<RefreshToken> findByUserIdAndRevokedFalse(UUID userId);

    /**
     * Revoke all refresh tokens for a user (used on password change / forced logout).
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId AND rt.revoked = false")
    int revokeAllUserTokens(@Param("userId") UUID userId);

    /**
     * Delete all expired refresh tokens (used by scheduled cleanup).
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < CURRENT_TIMESTAMP")
    int deleteAllExpired();
}
