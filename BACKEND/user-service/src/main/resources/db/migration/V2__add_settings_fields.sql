-- Phase 5 (Settings Dashboard): developer integrations + global card prefs.
--
-- These columns were added to the User entity but, because user-service had
-- no Flyway wiring yet, they were only ever applied via Hibernate's
-- ddl-auto=update. On databases where that never ran (or failed), reads such
-- as GET /api/users/me crashed with a missing-column SQL error.
--
-- ADD COLUMN IF NOT EXISTS keeps this safe on all environments: fresh
-- databases (V1 already ran), databases that never got the columns, and
-- databases where ddl-auto=update already added them.
ALTER TABLE users ADD COLUMN IF NOT EXISTS api_key           VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS webhook_url       VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS remove_watermark  BOOLEAN NOT NULL DEFAULT FALSE;
