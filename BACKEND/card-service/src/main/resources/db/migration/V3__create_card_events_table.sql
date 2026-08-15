-- Analytics event log for digital cards.
--
-- Every VIEW (page impression) and CLICK (social/portfolio link tapped) is
-- recorded here so the analytics endpoints can answer time-series questions
-- ("views per day", "clicks per link") that the cumulative view_count column
-- on card_profiles cannot.
CREATE TABLE card_events (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id  UUID NOT NULL REFERENCES card_profiles(id) ON DELETE CASCADE,
    event_type  VARCHAR(10) NOT NULL,
    visitor_id  VARCHAR(64),
    link_label  VARCHAR(100),
    event_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Aggregation-friendly indexes: daily views per profile, per-link clicks,
-- and distinct-visitor lookups.
CREATE INDEX idx_card_events_profile_event_at ON card_events(profile_id, event_type, event_at);
CREATE INDEX idx_card_events_visitor_id ON card_events(visitor_id);
