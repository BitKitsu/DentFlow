-- ============================================================
-- Identity Service: przykładowe dane (seed)
-- Hasło dla wszystkich użytkowników: password123
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S
-- ============================================================

-- -----------------------------------------------
-- Użytkownicy (powiązani z tenant_id z core-service)
-- -----------------------------------------------

-- Tenant 1: DentCare Kraków
INSERT INTO "user" (id, email, password_hash, tenant_id, status, created_at, updated_at) VALUES
(1, 'admin@dentcare.pl',         '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 1, 'ACTIVE', now(), now()),
(2, 'jan.kowalski@dentcare.pl',  '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 1, 'ACTIVE', now(), now()),
(3, 'anna.nowak@dentcare.pl',    '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 1, 'ACTIVE', now(), now()),
(4, 'marek.wisniewski@dentcare.pl','$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 1, 'ACTIVE', now(), now()),
(5, 'pacjent1@gmail.com',        '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 1, 'ACTIVE', now(), now()),
(6, 'pacjent2@gmail.com',        '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 1, 'ACTIVE', now(), now());

-- Tenant 2: SmileClinic Warszawa
INSERT INTO "user" (id, email, password_hash, tenant_id, status, created_at, updated_at) VALUES
(7, 'admin@smileclinic.pl',      '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 2, 'ACTIVE', now(), now()),
(8, 'ewa.zielinska@smileclinic.pl','$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 2, 'ACTIVE', now(), now()),
(9, 'piotr.kaczmarek@smileclinic.pl','$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 2, 'ACTIVE', now(), now()),
(10, 'pacjent3@gmail.com',       '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 2, 'ACTIVE', now(), now());

-- Reset sekwencji po ręcznym wstawieniu ID
SELECT setval(pg_get_serial_sequence('"user"', 'id'), (SELECT MAX(id) FROM "user"));

-- -----------------------------------------------
-- Role użytkowników
-- -----------------------------------------------
INSERT INTO user_role (user_id, role) VALUES
-- Tenant 1: DentCare
(1, 'ADMIN'),
(2, 'DENTIST'),
(3, 'DENTIST'),
(4, 'RECEPTIONIST'),
(5, 'PATIENT'),
(6, 'PATIENT'),
-- Tenant 2: SmileClinic
(7, 'ADMIN'),
(8, 'DENTIST'),
(9, 'RECEPTIONIST'),
(10, 'PATIENT');
