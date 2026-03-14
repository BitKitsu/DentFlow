-- Identity Service: schemat użytkowników i ról

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

CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_user_tenant ON "user"(tenant_id);
CREATE INDEX idx_user_role_user ON user_role(user_id);
