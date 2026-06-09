-- V7: Split display_name to first_name and last_name in staff_member

ALTER TABLE staff_member
    DROP COLUMN display_name,
    ADD COLUMN first_name VARCHAR(100) NOT NULL DEFAULT '',
    ADD COLUMN last_name VARCHAR(100) NOT NULL DEFAULT '';
