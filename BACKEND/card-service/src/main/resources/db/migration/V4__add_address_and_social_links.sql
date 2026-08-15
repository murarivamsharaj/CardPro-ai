-- Card schema expansion: physical address + flexible social media links.
--
-- address      — free-text office/physical address shown on the public card
-- social_links — JSONB map of platform key ("linkedin", "github", "twitter",
--                "instagram", "youtube", "website", "whatsapp", ...) -> URL.
--
-- card_events.event_type is widened because the public events endpoint stores
-- the full vocabulary (PAGE_VIEW, SOCIAL_CLICK, BUTTON_CLICK, VCF_DOWNLOAD)
-- which exceeds the original VARCHAR(10).
ALTER TABLE card_profiles ADD COLUMN address VARCHAR(500);
ALTER TABLE card_profiles ADD COLUMN social_links JSONB NOT NULL DEFAULT '{}'::jsonb;

ALTER TABLE card_events ALTER COLUMN event_type TYPE VARCHAR(20);

-- Backfill existing cards so the public viewer shows social links and address
-- without any client-side migration: pull the legacy profile_data fields into
-- the new columns. jsonb_strip_nulls drops the platforms the owner never set.
UPDATE card_profiles
SET social_links = jsonb_strip_nulls(jsonb_build_object(
        'linkedin', profile_data->>'linkedin',
        'github',   profile_data->>'github',
        'twitter',  profile_data->>'twitter',
        'instagram', profile_data->>'instagram',
        'youtube',  profile_data->>'youtube',
        'website',  COALESCE(profile_data->>'website', profile_data->>'portfolio'),
        'whatsapp', profile_data->>'whatsapp'
    ))
WHERE social_links = '{}'::jsonb
  AND (profile_data ? 'linkedin' OR profile_data ? 'github' OR profile_data ? 'twitter'
       OR profile_data ? 'instagram' OR profile_data ? 'youtube'
       OR profile_data ? 'website' OR profile_data ? 'portfolio' OR profile_data ? 'whatsapp');

UPDATE card_profiles
SET address = profile_data->>'address'
WHERE address IS NULL AND profile_data->>'address' IS NOT NULL;
