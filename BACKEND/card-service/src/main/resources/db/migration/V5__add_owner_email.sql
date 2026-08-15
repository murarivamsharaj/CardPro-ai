-- Card schema expansion: link each card back to its owner's user-service
-- profile (keyed by email, not the auth UUID) so the public card render can
-- honor the owner's preferences (currently: removeWatermark Pro perk).
ALTER TABLE card_profiles ADD COLUMN owner_email VARCHAR(255);

CREATE INDEX idx_card_profiles_owner_email ON card_profiles(owner_email);
