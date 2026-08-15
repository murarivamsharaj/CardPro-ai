package com.cardpro.card.repository;

import com.cardpro.card.entity.CardEvent;
import com.cardpro.card.entity.CardEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface CardEventRepository extends JpaRepository<CardEvent, UUID> {

    /**
     * Total views per calendar day (ISO {@code YYYY-MM-DD}) for the given
     * profiles, oldest first. Counts both the internal {@code VIEW} and public
     * {@code PAGE_VIEW} event types, at or after {@code since}. Returns rows of
     * {@code Object[]{String day, Number views}}.
     */
    @Query(value = """
            SELECT to_char(date_trunc('day', event_at), 'YYYY-MM-DD') AS day, COUNT(*) AS views
            FROM card_events
            WHERE profile_id IN (:profileIds)
              AND event_type IN ('VIEW', 'PAGE_VIEW')
              AND event_at >= :since
            GROUP BY day
            ORDER BY day
            """, nativeQuery = true)
    List<Object[]> countViewsByDay(@Param("profileIds") Collection<UUID> profileIds,
                                   @Param("since") LocalDateTime since);

    /**
     * Total clicks per link label for the given profiles, most-clicked first.
     * Aggregates every click-family event type (CLICK, SOCIAL_CLICK,
     * BUTTON_CLICK, VCF_DOWNLOAD). Rows are {@code Object[]{String linkLabel, Long clicks}}.
     */
    @Query("""
            SELECT e.linkLabel, COUNT(e)
            FROM CardEvent e
            WHERE e.profileId IN :profileIds
              AND e.eventType IN :clickTypes
              AND e.linkLabel IS NOT NULL
            GROUP BY e.linkLabel
            ORDER BY COUNT(e) DESC
            """)
    List<Object[]> countClicksByLink(@Param("profileIds") Collection<UUID> profileIds,
                                     @Param("clickTypes") Collection<CardEventType> clickTypes);

    /**
     * Number of distinct visitors (non-null {@code visitorId}) among the given
     * profiles' view-family events (VIEW + PAGE_VIEW). Used for the
     * unique-visitor stat.
     */
    @Query("""
            SELECT COUNT(DISTINCT e.visitorId)
            FROM CardEvent e
            WHERE e.profileId IN :profileIds
              AND e.eventType IN :viewTypes
              AND e.visitorId IS NOT NULL
            """)
    long countDistinctVisitors(@Param("profileIds") Collection<UUID> profileIds,
                               @Param("viewTypes") Collection<CardEventType> viewTypes);

    /**
     * Total events of the given types since a cutoff — used for the admin
     * "views in the last 7 days" metric across the whole platform (counts both
     * the internal VIEW and public PAGE_VIEW event types).
     */
    @Query("""
            SELECT COUNT(e)
            FROM CardEvent e
            WHERE e.eventType IN :types
              AND e.eventAt >= :since
            """)
    long countEventsSince(@Param("types") Collection<CardEventType> types,
                          @Param("since") LocalDateTime since);
}
