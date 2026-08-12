-- The public "Contact Me" form captures Name, Email and Message but no phone,
-- so visitor_phone must be nullable.
ALTER TABLE leads
    ALTER COLUMN visitor_phone DROP NOT NULL;
