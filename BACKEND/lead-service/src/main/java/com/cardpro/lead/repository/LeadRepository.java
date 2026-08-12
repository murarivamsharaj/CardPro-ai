package com.cardpro.lead.repository;

import com.cardpro.lead.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    long countByProfileId(UUID profileId);
}
