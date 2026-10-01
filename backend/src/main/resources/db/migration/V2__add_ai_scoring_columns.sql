-- Add missing tables and columns to align with JPA entities

-- 1. Fix applications table
ALTER TABLE applications 
ADD COLUMN IF NOT EXISTS ai_match_score DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS ai_rationale TEXT,
ADD COLUMN IF NOT EXISTS recruiter_notes TEXT,
ADD COLUMN IF NOT EXISTS interview_date TIMESTAMP;

-- Drop old columns if they exist
ALTER TABLE applications 
DROP COLUMN IF EXISTS match_score,
DROP COLUMN IF EXISTS match_breakdown_json;

-- 2. Fix audit_logs table - rename actor_username to actor_email
ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS actor_email VARCHAR(120);

UPDATE audit_logs SET actor_email = actor_username 
WHERE actor_email IS NULL AND actor_username IS NOT NULL;

UPDATE audit_logs SET actor_email = 'SYSTEM' 
WHERE actor_email IS NULL;

ALTER TABLE audit_logs 
ALTER COLUMN actor_email SET NOT NULL;

ALTER TABLE audit_logs 
DROP COLUMN IF EXISTS actor_username;

-- Add missing columns to audit_logs
ALTER TABLE audit_logs 
ADD COLUMN IF NOT EXISTS resource_type VARCHAR(60),
ADD COLUMN IF NOT EXISTS resource_id VARCHAR(80),
ADD COLUMN IF NOT EXISTS result VARCHAR(20) DEFAULT 'SUCCESS',
ADD COLUMN IF NOT EXISTS metadata TEXT,
ADD COLUMN IF NOT EXISTS ip_address VARCHAR(64);

ALTER TABLE audit_logs 
ALTER COLUMN action TYPE VARCHAR(80);

ALTER TABLE audit_logs 
ALTER COLUMN action SET NOT NULL,
ALTER COLUMN result SET NOT NULL,
ALTER COLUMN timestamp SET NOT NULL;

-- 3. Create missing candidate_certifications table
CREATE TABLE IF NOT EXISTS candidate_certifications (
    id BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    issuing_organization VARCHAR(255),
    issue_date DATE,
    expiration_date DATE,
    credential_id VARCHAR(255),
    credential_url VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_candidate_certs_candidate ON candidate_certifications(candidate_id);
