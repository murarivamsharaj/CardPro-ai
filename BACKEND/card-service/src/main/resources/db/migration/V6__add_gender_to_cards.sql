-- Adds the optional gender field to digital cards (Male / Female / Custom / Prefer not to say).
ALTER TABLE card_profiles ADD COLUMN gender VARCHAR(50);
