-- Add logo_url to tenant
ALTER TABLE tenant ADD COLUMN IF NOT EXISTS logo_url VARCHAR(1000);

-- Add public_url to file_metadata
ALTER TABLE file_metadata ADD COLUMN IF NOT EXISTS public_url VARCHAR(1000);
