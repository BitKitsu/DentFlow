-- Make tennat_id optional so users can uploat avatar without tenant_id
ALTER TABLE file_metadata ALTER COLUMN tenant_id DROP NOT NULL;
