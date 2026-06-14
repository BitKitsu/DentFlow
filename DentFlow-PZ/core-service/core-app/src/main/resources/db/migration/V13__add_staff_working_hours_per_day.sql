CREATE TABLE IF NOT EXISTS staff_working_hours (
    id          BIGSERIAL PRIMARY KEY,
    staff_id    BIGINT NOT NULL REFERENCES staff_member(id) ON DELETE CASCADE,
    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time  TIME NOT NULL DEFAULT '08:00',
    end_time    TIME NOT NULL DEFAULT '16:00',
    UNIQUE(staff_id, day_of_week)
);

CREATE INDEX idx_swh_staff ON staff_working_hours(staff_id);
