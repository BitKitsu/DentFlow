-- V6: Add bio field to staff_member table

ALTER TABLE staff_member
    ADD COLUMN IF NOT EXISTS bio TEXT;
