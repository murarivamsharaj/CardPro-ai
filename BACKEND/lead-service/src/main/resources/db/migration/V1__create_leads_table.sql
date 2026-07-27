CREATE TABLE leads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL,
    visitor_name VARCHAR(150) NOT NULL,
    visitor_phone VARCHAR(20) NOT NULL,
    ai_followup TEXT,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_leads_profile_id ON leads(profile_id);
CREATE INDEX idx_leads_captured_at ON leads(captured_at);
