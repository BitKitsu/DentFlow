-- Join table: staff_room (doctor-room assignment)
CREATE TABLE IF NOT EXISTS staff_room (
    id          BIGSERIAL PRIMARY KEY,
    staff_id    BIGINT NOT NULL REFERENCES staff_member(id) ON DELETE CASCADE,
    room_id     BIGINT NOT NULL REFERENCES room(id) ON DELETE CASCADE,
    UNIQUE(staff_id, room_id)
);
CREATE INDEX idx_staff_room_staff ON staff_room(staff_id);
CREATE INDEX idx_staff_room_room  ON staff_room(room_id);

-- Allow patient_id to be NULL on appointment (for walk-in patients)
ALTER TABLE appointment ALTER COLUMN patient_id DROP NOT NULL;
