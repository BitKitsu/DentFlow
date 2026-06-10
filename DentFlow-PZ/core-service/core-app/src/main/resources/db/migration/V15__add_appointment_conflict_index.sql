-- Composite index for appointment conflict detection query
-- Covers: WHERE tenant_id = ? AND dentist_staff_id = ? AND status NOT IN ('CANCELLED') AND start_at < ? AND end_at > ?
CREATE INDEX IF NOT EXISTS idx_appointment_conflict
    ON appointment(tenant_id, dentist_staff_id, status, start_at, end_at);
