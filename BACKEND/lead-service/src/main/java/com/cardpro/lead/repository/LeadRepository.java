package com.cardpro.lead.repository;

import com.cardpro.lead.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {

    Page<Lead> findByProfileId(UUID profileId, Pageable pageable);

    /**
     * All leads captured against any of the given card (profile) ids, newest
     * first. Used by the dashboard to scope leads to the logged-in user's cards.
     */
    Page<Lead> findByProfileIdInOrderByCapturedAtDesc(Collection<UUID> profileIds, Pageable pageable);

    /**
     * Same as {@link #findByProfileIdInOrderByCapturedAtDesc} but filtered by a
     * case-insensitive search across the visitor's name, email and phone.
     *
     * <p>Bulletproof against a {@code null} <em>or</em> empty {@code search}:
     * both are treated as a no-op filter (matches every row), so callers never
     * hit a malformed {@code LIKE '%%NULL%%'} clause and PostgreSQL never
     * errors on a bound null/empty parameter.</p>
     */
    @Query("""
            SELECT l FROM Lead l
            WHERE l.profileId IN :profileIds
              AND (:search IS NULL
                   OR :search = ''
                   OR LOWER(l.visitorName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(l.visitorEmail) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(l.visitorPhone) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY l.capturedAt DESC
            """)
    Page<Lead> searchByProfileIds(@Param("profileIds") Collection<UUID> profileIds,
                                  @Param("search") String search,
                                  Pageable pageable);

    long countByProfileId(UUID profileId);
}
