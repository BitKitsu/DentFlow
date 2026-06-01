-- V5: Add date_of_birth to patient table

ALTER TABLE patient
    ADD COLUMN IF NOT EXISTS date_of_birth DATE;
