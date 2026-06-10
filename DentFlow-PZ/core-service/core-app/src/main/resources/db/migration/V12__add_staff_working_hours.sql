ALTER TABLE staff_member ADD COLUMN IF NOT EXISTS working_hours_start TIME DEFAULT '08:00:00';
ALTER TABLE staff_member ADD COLUMN IF NOT EXISTS working_hours_end TIME DEFAULT '16:00:00';
