CREATE TABLE card_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    template_id VARCHAR(50) NOT NULL DEFAULT 'basic',
    profile_data JSONB NOT NULL,
    ai_avatar_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_card_profiles_user_id ON card_profiles(user_id);
CREATE UNIQUE INDEX idx_card_profiles_slug ON card_profiles(slug);
CREATE INDEX idx_card_profiles_template_id ON card_profiles(template_id);
