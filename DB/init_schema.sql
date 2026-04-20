-- ============================================================
-- DentFlow - Skrypt tworzący strukturę bazy danych (BEZ DANYCH)
-- Plik do umieszczenia w repozytorium (folder DB/)
-- ============================================================

-- ##############################################################
-- SCHEMAT IDENTITY SERVICE
-- ##############################################################

CREATE TABLE IF NOT EXISTS "user" (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    tenant_id       BIGINT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_role (
    id       BIGSERIAL PRIMARY KEY,
    user_id  BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    role     VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_user_email ON "user"(email);
CREATE INDEX IF NOT EXISTS idx_user_tenant ON "user"(tenant_id);
CREATE INDEX IF NOT EXISTS idx_user_role_user ON user_role(user_id);

-- ##############################################################
-- SCHEMAT CORE SERVICE
-- ##############################################################

-- tenant (gabinet stomatologiczny = tenant SaaS)
CREATE TABLE IF NOT EXISTS tenant (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- location (lokalizacje/placówki gabinetu)
CREATE TABLE IF NOT EXISTS location (
    id               BIGSERIAL    PRIMARY KEY,
    tenant_id        BIGINT       NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    name             VARCHAR(100) NOT NULL,
    address_street   VARCHAR(100),
    address_city     VARCHAR(100),
    address_zip      VARCHAR(20),
    address_country  VARCHAR(50)
);
CREATE INDEX IF NOT EXISTS idx_location_tenant ON location(tenant_id);

-- room (gabinety/pomieszczenia w lokalizacji)
CREATE TABLE IF NOT EXISTS room (
    id          BIGSERIAL    PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    location_id BIGINT       NOT NULL REFERENCES location(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_room_tenant   ON room(tenant_id);
CREATE INDEX IF NOT EXISTS idx_room_location ON room(location_id);

-- staff_member (pracownicy gabinetu)
CREATE TABLE IF NOT EXISTS staff_member (
    id           BIGSERIAL    PRIMARY KEY,
    tenant_id    BIGINT       NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    user_id      BIGINT,
    display_name VARCHAR(100) NOT NULL,
    profession   VARCHAR(20)  NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_staff_tenant  ON staff_member(tenant_id);
CREATE INDEX IF NOT EXISTS idx_staff_user    ON staff_member(user_id);

-- patient (pacjenci)
CREATE TABLE IF NOT EXISTS patient (
    id         BIGSERIAL    PRIMARY KEY,
    tenant_id  BIGINT       NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    user_id    BIGINT,
    first_name VARCHAR(50)  NOT NULL,
    last_name  VARCHAR(50)  NOT NULL,
    phone      VARCHAR(20),
    email      VARCHAR(255),
    notes      TEXT
);
CREATE INDEX IF NOT EXISTS idx_patient_tenant ON patient(tenant_id);
CREATE INDEX IF NOT EXISTS idx_patient_user   ON patient(user_id);

-- service_catalog_item (cennik usług)
CREATE TABLE IF NOT EXISTS service_catalog_item (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT    NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    name             VARCHAR(100) NOT NULL,
    duration_minutes INT       NOT NULL CHECK (duration_minutes > 0),
    price_cents      INT       NOT NULL CHECK (price_cents >= 0),
    active           BOOLEAN   NOT NULL DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS idx_catalog_tenant ON service_catalog_item(tenant_id);

-- work_schedule_slot (sloty grafiku pracy dentysty)
CREATE TABLE IF NOT EXISTS work_schedule_slot (
    id          BIGSERIAL   PRIMARY KEY,
    tenant_id   BIGINT      NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    staff_id    BIGINT      NOT NULL REFERENCES staff_member(id) ON DELETE CASCADE,
    location_id BIGINT      NOT NULL REFERENCES location(id) ON DELETE CASCADE,
    room_id     BIGINT      REFERENCES room(id) ON DELETE SET NULL,
    start_at    TIMESTAMPTZ NOT NULL,
    end_at      TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_wss_tenant  ON work_schedule_slot(tenant_id);
CREATE INDEX IF NOT EXISTS idx_wss_staff   ON work_schedule_slot(staff_id);
CREATE INDEX IF NOT EXISTS idx_wss_slot    ON work_schedule_slot(start_at, end_at);

-- blocker (blokady czasu: urlopy, niedostępność)
CREATE TABLE IF NOT EXISTS blocker (
    id        BIGSERIAL    PRIMARY KEY,
    tenant_id BIGINT       NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    staff_id  BIGINT       REFERENCES staff_member(id) ON DELETE CASCADE,
    room_id   BIGINT       REFERENCES room(id) ON DELETE SET NULL,
    start_at  TIMESTAMPTZ  NOT NULL,
    end_at    TIMESTAMPTZ  NOT NULL,
    reason    VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_blocker_tenant ON blocker(tenant_id);
CREATE INDEX IF NOT EXISTS idx_blocker_staff  ON blocker(staff_id);

-- appointment (wizyty)
CREATE TABLE IF NOT EXISTS appointment (
    id               BIGSERIAL    PRIMARY KEY,
    tenant_id        BIGINT       NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    location_id      BIGINT       NOT NULL REFERENCES location(id),
    room_id          BIGINT       REFERENCES room(id) ON DELETE SET NULL,
    dentist_staff_id BIGINT       NOT NULL REFERENCES staff_member(id),
    patient_id       BIGINT       NOT NULL REFERENCES patient(id),
    service_item_id  BIGINT       REFERENCES service_catalog_item(id) ON DELETE SET NULL,
    start_at         TIMESTAMPTZ  NOT NULL,
    end_at           TIMESTAMPTZ  NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    created_by_user_id BIGINT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    notes            TEXT
);
CREATE INDEX IF NOT EXISTS idx_appointment_tenant   ON appointment(tenant_id);
CREATE INDEX IF NOT EXISTS idx_appointment_dentist  ON appointment(dentist_staff_id);
CREATE INDEX IF NOT EXISTS idx_appointment_patient  ON appointment(patient_id);
CREATE INDEX IF NOT EXISTS idx_appointment_time     ON appointment(start_at, end_at);

-- notification (powiadomienia in-app)
CREATE TABLE IF NOT EXISTS notification (
    id         BIGSERIAL    PRIMARY KEY,
    tenant_id  BIGINT       NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    user_id    BIGINT       NOT NULL,
    type       VARCHAR(50)  NOT NULL,
    message    TEXT         NOT NULL,
    read       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_notification_user   ON notification(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_tenant ON notification(tenant_id);

-- file_metadata (metadane plików w Supabase Storage)
CREATE TABLE IF NOT EXISTS file_metadata (
    id            BIGSERIAL    PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    original_name VARCHAR(255) NOT NULL,
    storage_path  VARCHAR(500) NOT NULL,
    content_type  VARCHAR(100),
    size_bytes    BIGINT,
    uploaded_by   BIGINT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_file_tenant ON file_metadata(tenant_id);

-- audit_log (log zmian)
CREATE TABLE IF NOT EXISTS audit_log (
    id           BIGSERIAL   PRIMARY KEY,
    tenant_id    BIGINT      NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    entity_name  VARCHAR(50) NOT NULL,
    entity_id    BIGINT      NOT NULL,
    action       VARCHAR(20) NOT NULL CHECK (action IN ('CREATE','UPDATE','DELETE')),
    performed_by BIGINT,
    timestamp    TIMESTAMPTZ NOT NULL DEFAULT now(),
    details      JSONB
);
CREATE INDEX IF NOT EXISTS idx_audit_tenant ON audit_log(tenant_id);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_log(entity_name, entity_id);
