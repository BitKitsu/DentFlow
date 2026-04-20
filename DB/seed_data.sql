-- ============================================================
-- DentFlow - Przykładowe dane (TYLKO INSERT, bez schematu)
-- Wymaga wcześniejszego uruchomienia init_schema.sql
-- ============================================================
-- Hasło dla wszystkich użytkowników: password123
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S

-- === TENANT ===
INSERT INTO tenant (id, name, status, created_at) VALUES
(1, 'DentCare Kraków',      'ACTIVE', '2025-01-15 10:00:00+01'),
(2, 'SmileClinic Warszawa', 'ACTIVE', '2025-03-01 09:00:00+01');

-- === USER ===
INSERT INTO "user" (id, email, password_hash, tenant_id, status) VALUES
(1,  'admin@dentcare.pl',              '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 1, 'ACTIVE'),
(2,  'jan.kowalski@dentcare.pl',       '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 1, 'ACTIVE'),
(3,  'anna.nowak@dentcare.pl',         '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 1, 'ACTIVE'),
(4,  'marek.wisniewski@dentcare.pl',   '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 1, 'ACTIVE'),
(5,  'pacjent1@gmail.com',             '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 1, 'ACTIVE'),
(6,  'pacjent2@gmail.com',             '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 1, 'ACTIVE'),
(7,  'admin@smileclinic.pl',           '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 2, 'ACTIVE'),
(8,  'ewa.zielinska@smileclinic.pl',   '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 2, 'ACTIVE'),
(9,  'piotr.kaczmarek@smileclinic.pl', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 2, 'ACTIVE'),
(10, 'pacjent3@gmail.com',             '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHKPaO0/v7CjFYt2IWNSEy6S', 2, 'ACTIVE');

-- === USER_ROLE ===
INSERT INTO user_role (user_id, role) VALUES
(1, 'ADMIN'), (2, 'DENTIST'), (3, 'DENTIST'), (4, 'RECEPTIONIST'),
(5, 'PATIENT'), (6, 'PATIENT'),
(7, 'ADMIN'), (8, 'DENTIST'), (9, 'RECEPTIONIST'), (10, 'PATIENT');

-- === LOCATION ===
INSERT INTO location (id, tenant_id, name, address_street, address_city, address_zip, address_country) VALUES
(1, 1, 'DentCare Centrum',    'ul. Floriańska 15', 'Kraków',   '31-019', 'Polska'),
(2, 1, 'DentCare Nowa Huta',  'os. Centrum B 1',   'Kraków',   '31-929', 'Polska'),
(3, 2, 'SmileClinic Mokotów', 'ul. Puławska 112',  'Warszawa', '02-620', 'Polska'),
(4, 2, 'SmileClinic Wola',    'ul. Kasprzaka 25',  'Warszawa', '01-211', 'Polska');

-- === ROOM ===
INSERT INTO room (id, tenant_id, location_id, name) VALUES
(1, 1, 1, 'Gabinet 1 - Stomatologia zachowawcza'),
(2, 1, 1, 'Gabinet 2 - Chirurgia'),
(3, 1, 1, 'Gabinet 3 - Ortodoncja'),
(4, 1, 2, 'Gabinet 1 - Ogólny'),
(5, 1, 2, 'Gabinet 2 - Protetyka'),
(6, 2, 3, 'Gabinet A'),
(7, 2, 3, 'Gabinet B'),
(8, 2, 4, 'Gabinet Główny');

-- === STAFF_MEMBER ===
INSERT INTO staff_member (id, tenant_id, user_id, display_name, profession) VALUES
(1, 1, 2, 'dr Jan Kowalski',  'DENTIST'),
(2, 1, 3, 'dr Anna Nowak',    'DENTIST'),
(3, 1, 4, 'Marek Wiśniewski', 'RECEPTIONIST'),
(4, 2, 8, 'dr Ewa Zielińska', 'DENTIST'),
(5, 2, 9, 'Piotr Kaczmarek',  'RECEPTIONIST');

-- === PATIENT ===
INSERT INTO patient (id, tenant_id, user_id, first_name, last_name, phone, email, notes) VALUES
(1,  1, 5,    'Tomasz',    'Lewandowski', '+48 600 111 222', 'pacjent1@gmail.com',   'Alergia na lidokainę'),
(2,  1, 6,    'Katarzyna', 'Wójcik',      '+48 601 222 333', 'pacjent2@gmail.com',   NULL),
(3,  1, NULL, 'Michał',    'Kamiński',    '+48 602 333 444', 'michal.k@wp.pl',       'Stały pacjent od 2023'),
(4,  1, NULL, 'Agnieszka', 'Szymańska',   '+48 603 444 555', 'a.szymanska@o2.pl',    NULL),
(5,  1, NULL, 'Paweł',     'Dąbrowski',   '+48 604 555 666', NULL,                    'Pacjent z polecenia'),
(6,  1, NULL, 'Magdalena', 'Kozłowska',   '+48 605 666 777', 'magda.kozl@gmail.com', NULL),
(7,  2, 10,   'Robert',    'Jankowski',   '+48 700 111 222', 'pacjent3@gmail.com',   NULL),
(8,  2, NULL, 'Monika',    'Mazur',       '+48 701 222 333', 'monika.m@gmail.com',   'Bruksizm - szyna relaksacyjna'),
(9,  2, NULL, 'Krzysztof', 'Krawczyk',    '+48 702 333 444', 'k.krawczyk@wp.pl',     NULL),
(10, 2, NULL, 'Joanna',    'Piotrowska',  '+48 703 444 555', 'jpiotrowska@o2.pl',    'Ciąża - II trymestr');

-- === SERVICE_CATALOG_ITEM ===
INSERT INTO service_catalog_item (id, tenant_id, name, duration_minutes, price_cents, active) VALUES
(1,  1, 'Przegląd stomatologiczny',          30,  15000, TRUE),
(2,  1, 'Skaling (usuwanie kamienia)',        45,  20000, TRUE),
(3,  1, 'Wypełnienie kompozytowe (1 ząb)',    60,  25000, TRUE),
(4,  1, 'Leczenie kanałowe (1 kanał)',        90,  50000, TRUE),
(5,  1, 'Ekstrakcja zęba',                    45,  30000, TRUE),
(6,  1, 'Wybielanie zębów (nakładkowe)',      60,  80000, TRUE),
(7,  1, 'Licówka porcelanowa',                90, 200000, TRUE),
(8,  1, 'Konsultacja ortodontyczna',          30,  10000, TRUE),
(9,  1, 'Aparat ortodontyczny (założenie)',  120, 350000, TRUE),
(10, 1, 'RTG pantomograficzne',               15,   8000, TRUE),
(11, 1, 'Korona porcelanowa',                 90, 150000, TRUE),
(12, 1, 'Most protetyczny (3-punktowy)',     120, 400000, FALSE),
(13, 2, 'Konsultacja stomatologiczna',        30,  10000, TRUE),
(14, 2, 'Higienizacja (skaling + piaskowanie)',60, 25000, TRUE),
(15, 2, 'Wypełnienie światłoutwardzalne',     45,  22000, TRUE),
(16, 2, 'Endodoncja mikroskopowa',           120,  80000, TRUE),
(17, 2, 'Implant zębowy',                    120, 500000, TRUE),
(18, 2, 'Wybielanie BEYOND',                  90, 120000, TRUE),
(19, 2, 'Ekstrakcja zęba mądrości',           60,  45000, TRUE),
(20, 2, 'Szyna relaksacyjna',                 45,  60000, TRUE);

-- === WORK_SCHEDULE_SLOT ===
INSERT INTO work_schedule_slot (id, tenant_id, staff_id, location_id, room_id, start_at, end_at) VALUES
(1,  1, 1, 1, 1, '2026-04-20 08:00+02', '2026-04-20 16:00+02'),
(2,  1, 1, 1, 1, '2026-04-21 08:00+02', '2026-04-21 16:00+02'),
(3,  1, 1, 1, 1, '2026-04-22 08:00+02', '2026-04-22 16:00+02'),
(4,  1, 1, 2, 4, '2026-04-23 10:00+02', '2026-04-23 18:00+02'),
(5,  1, 1, 2, 4, '2026-04-24 10:00+02', '2026-04-24 18:00+02'),
(6,  1, 2, 1, 2, '2026-04-20 09:00+02', '2026-04-20 17:00+02'),
(7,  1, 2, 1, 2, '2026-04-21 09:00+02', '2026-04-21 17:00+02'),
(8,  1, 2, 1, 2, '2026-04-22 09:00+02', '2026-04-22 17:00+02'),
(9,  1, 2, 1, 2, '2026-04-23 09:00+02', '2026-04-23 17:00+02'),
(10, 1, 2, 1, 2, '2026-04-24 09:00+02', '2026-04-24 17:00+02'),
(11, 2, 4, 3, 6, '2026-04-20 08:00+02', '2026-04-20 14:00+02'),
(12, 2, 4, 3, 6, '2026-04-21 08:00+02', '2026-04-21 14:00+02'),
(13, 2, 4, 3, 6, '2026-04-22 08:00+02', '2026-04-22 14:00+02'),
(14, 2, 4, 3, 6, '2026-04-23 08:00+02', '2026-04-23 14:00+02'),
(15, 2, 4, 4, 8, '2026-04-24 10:00+02', '2026-04-24 18:00+02');

-- === BLOCKER ===
INSERT INTO blocker (id, tenant_id, staff_id, room_id, start_at, end_at, reason) VALUES
(1, 1, 1, NULL, '2026-04-22 13:00+02', '2026-04-22 16:00+02', 'Szkolenie - nowe materiały kompozytowe'),
(2, 1, NULL, 2,  '2026-04-24 08:00+02', '2026-04-24 12:00+02', 'Przegląd techniczny unitów'),
(3, 2, 4, NULL,  '2026-04-23 12:00+02', '2026-04-23 14:00+02', 'Wizyta lekarska');

-- === APPOINTMENT ===
INSERT INTO appointment (id, tenant_id, location_id, room_id, dentist_staff_id, patient_id, service_item_id, start_at, end_at, status, created_by_user_id, created_at, updated_at, notes) VALUES
(1,  1, 1, 1, 1, 1, 1,  '2026-04-20 08:00+02', '2026-04-20 08:30+02', 'COMPLETED', 4, '2026-04-15 10:00+02', '2026-04-20 08:35+02', 'Przegląd ok, zalecany skaling'),
(2,  1, 1, 1, 1, 2, 3,  '2026-04-20 09:00+02', '2026-04-20 10:00+02', 'COMPLETED', 4, '2026-04-16 14:00+02', '2026-04-20 10:05+02', 'Wypełnienie ząb 36, kompozyt A3'),
(3,  1, 1, 1, 1, 3, 2,  '2026-04-20 10:30+02', '2026-04-20 11:15+02', 'COMPLETED', 4, '2026-04-17 09:00+02', '2026-04-20 11:20+02', NULL),
(4,  1, 1, 2, 2, 4, 4,  '2026-04-20 09:00+02', '2026-04-20 10:30+02', 'COMPLETED', 4, '2026-04-14 11:00+02', '2026-04-20 10:35+02', 'Kanał 1 - ząb 46'),
(5,  1, 1, 2, 2, 5, 1,  '2026-04-20 11:00+02', '2026-04-20 11:30+02', 'NO_SHOW',   4, '2026-04-18 16:00+02', '2026-04-20 11:45+02', 'Pacjent nie stawił się'),
(6,  1, 1, 1, 1, 6, 6,  '2026-04-21 08:00+02', '2026-04-21 09:00+02', 'SCHEDULED', 4, '2026-04-19 10:00+02', '2026-04-19 10:00+02', 'Wybielanie - pierwsza wizyta'),
(7,  1, 1, 1, 1, 1, 2,  '2026-04-21 09:30+02', '2026-04-21 10:15+02', 'SCHEDULED', 4, '2026-04-19 11:00+02', '2026-04-19 11:00+02', NULL),
(8,  1, 1, 2, 2, 3, 11, '2026-04-21 09:00+02', '2026-04-21 10:30+02', 'SCHEDULED', 4, '2026-04-18 09:00+02', '2026-04-18 09:00+02', 'Korona na ząb 14'),
(9,  1, 1, 2, 2, 4, 4,  '2026-04-21 11:00+02', '2026-04-21 12:30+02', 'SCHEDULED', 4, '2026-04-20 10:40+02', '2026-04-20 10:40+02', 'Kontynuacja kanałowego 46'),
(10, 1, 1, 1, 1, 2, 10, '2026-04-22 08:00+02', '2026-04-22 08:15+02', 'SCHEDULED', 4, '2026-04-20 14:00+02', '2026-04-20 14:00+02', 'RTG kontrolne'),
(11, 1, 1, 1, 1, 5, 1,  '2026-04-22 08:30+02', '2026-04-22 09:00+02', 'SCHEDULED', 4, '2026-04-20 15:00+02', '2026-04-20 15:00+02', 'Ponowna wizyta po no-show'),
(12, 1, 2, 4, 1, 3, 5,  '2026-04-23 10:00+02', '2026-04-23 10:45+02', 'SCHEDULED', 4, '2026-04-19 08:00+02', '2026-04-19 08:00+02', 'Ekstrakcja 48'),
(13, 1, 2, 4, 1, 6, 1,  '2026-04-23 11:00+02', '2026-04-23 11:30+02', 'SCHEDULED', 4, '2026-04-20 09:00+02', '2026-04-20 09:00+02', NULL),
(14, 1, 2, 4, 1, 1, 8,  '2026-04-24 10:00+02', '2026-04-24 10:30+02', 'SCHEDULED', 4, '2026-04-20 12:00+02', '2026-04-20 12:00+02', 'Konsultacja ortodontyczna'),
(15, 1, 1, 1, 1, 4, 7,  '2026-04-24 14:00+02', '2026-04-24 15:30+02', 'CANCELLED', 4, '2026-04-15 16:00+02', '2026-04-19 09:00+02', 'Anulowana przez pacjentkę'),
(16, 2, 3, 6, 4, 7,  13, '2026-04-20 08:00+02', '2026-04-20 08:30+02', 'COMPLETED', 9, '2026-04-14 10:00+02', '2026-04-20 08:35+02', 'Konsultacja przed implantacją'),
(17, 2, 3, 6, 4, 8,  20, '2026-04-20 09:00+02', '2026-04-20 09:45+02', 'COMPLETED', 9, '2026-04-15 11:00+02', '2026-04-20 09:50+02', 'Wycisk pod szynę'),
(18, 2, 3, 6, 4, 9,  14, '2026-04-20 10:00+02', '2026-04-20 11:00+02', 'COMPLETED', 9, '2026-04-16 09:00+02', '2026-04-20 11:05+02', NULL),
(19, 2, 3, 6, 4, 10, 13, '2026-04-21 08:00+02', '2026-04-21 08:30+02', 'SCHEDULED', 9, '2026-04-19 14:00+02', '2026-04-19 14:00+02', 'Ciąża - bezpieczne zabiegi'),
(20, 2, 3, 6, 4, 7,  17, '2026-04-21 09:00+02', '2026-04-21 11:00+02', 'SCHEDULED', 9, '2026-04-20 08:40+02', '2026-04-20 08:40+02', 'Implant 36 - etap I'),
(21, 2, 3, 6, 4, 8,  20, '2026-04-22 08:00+02', '2026-04-22 08:45+02', 'SCHEDULED', 9, '2026-04-20 09:55+02', '2026-04-20 09:55+02', 'Oddanie szyny relaksacyjnej'),
(22, 2, 3, 6, 4, 9,  15, '2026-04-22 09:00+02', '2026-04-22 09:45+02', 'SCHEDULED', 9, '2026-04-18 10:00+02', '2026-04-18 10:00+02', 'Wypełnienie 25'),
(23, 2, 4, 8, 4, 7,  18, '2026-04-24 10:00+02', '2026-04-24 11:30+02', 'SCHEDULED', 9, '2026-04-20 08:45+02', '2026-04-20 08:45+02', 'Wybielanie BEYOND'),
(24, 2, 4, 8, 4, 10, 13, '2026-04-24 12:00+02', '2026-04-24 12:30+02', 'SCHEDULED', 9, '2026-04-19 15:00+02', '2026-04-19 15:00+02', 'Kontrola - ciąża');

-- === NOTIFICATION ===
INSERT INTO notification (id, tenant_id, user_id, type, message, read, created_at) VALUES
(1, 1, 5,  'APPOINTMENT_REMINDER',  'Przypomnienie: wizyta jutro o 09:30 - Skaling u dr. Kowalskiego', FALSE, '2026-04-20 18:00+02'),
(2, 1, 6,  'APPOINTMENT_REMINDER',  'Przypomnienie: wizyta jutro o 08:00 - Wybielanie u dr. Kowalskiego', FALSE, '2026-04-20 18:00+02'),
(3, 1, 5,  'APPOINTMENT_CONFIRMED', 'Twoja wizyta 20.04 o 08:00 została potwierdzona', TRUE, '2026-04-15 10:05+02'),
(4, 1, 2,  'SCHEDULE_CHANGE',       'Blokada: szkolenie 22.04 13:00-16:00', TRUE, '2026-04-18 09:00+02'),
(5, 1, 4,  'APPOINTMENT_NO_SHOW',   'Pacjent Paweł Dąbrowski nie stawił się na wizytę 20.04 11:00', FALSE, '2026-04-20 11:45+02'),
(6, 2, 10, 'APPOINTMENT_REMINDER',  'Przypomnienie: wizyta jutro o 08:00 - Konsultacja w SmileClinic', FALSE, '2026-04-20 18:00+02'),
(7, 2, 10, 'APPOINTMENT_CONFIRMED', 'Twoja wizyta 24.04 o 12:00 została potwierdzona', TRUE, '2026-04-19 15:05+02'),
(8, 2, 8,  'APPOINTMENT_REMINDER',  'Przypomnienie: wizyta 22.04 o 08:00 - Odbiór szyny relaksacyjnej', FALSE, '2026-04-21 18:00+02');

-- === AUDIT_LOG ===
INSERT INTO audit_log (id, tenant_id, entity_name, entity_id, action, performed_by, timestamp, details) VALUES
(1, 1, 'PATIENT',     1, 'CREATE', 4, '2026-04-10 10:00+02', '{"firstName":"Tomasz","lastName":"Lewandowski"}'::jsonb),
(2, 1, 'PATIENT',     2, 'CREATE', 4, '2026-04-10 10:30+02', '{"firstName":"Katarzyna","lastName":"Wójcik"}'::jsonb),
(3, 1, 'APPOINTMENT', 1, 'CREATE', 4, '2026-04-15 10:00+02', '{"patientId":1,"dentistStaffId":1,"serviceItemId":1}'::jsonb),
(4, 1, 'APPOINTMENT', 1, 'UPDATE', 2, '2026-04-20 08:35+02', '{"status":"COMPLETED","previousStatus":"SCHEDULED"}'::jsonb),
(5, 1, 'APPOINTMENT', 5, 'UPDATE', 4, '2026-04-20 11:45+02', '{"status":"NO_SHOW","previousStatus":"SCHEDULED"}'::jsonb),
(6, 1, 'APPOINTMENT',15, 'UPDATE', 4, '2026-04-19 09:00+02', '{"status":"CANCELLED","previousStatus":"SCHEDULED"}'::jsonb),
(7, 2, 'PATIENT',     7, 'CREATE', 9, '2026-04-12 09:00+02', '{"firstName":"Robert","lastName":"Jankowski"}'::jsonb),
(8, 2, 'APPOINTMENT',16, 'UPDATE', 8, '2026-04-20 08:35+02', '{"status":"COMPLETED","previousStatus":"SCHEDULED"}'::jsonb);

-- === RESET SEKWENCJI ===
SELECT setval(pg_get_serial_sequence('"user"', 'id'), (SELECT MAX(id) FROM "user"));
SELECT setval(pg_get_serial_sequence('tenant', 'id'), (SELECT MAX(id) FROM tenant));
SELECT setval(pg_get_serial_sequence('location', 'id'), (SELECT MAX(id) FROM location));
SELECT setval(pg_get_serial_sequence('room', 'id'), (SELECT MAX(id) FROM room));
SELECT setval(pg_get_serial_sequence('staff_member', 'id'), (SELECT MAX(id) FROM staff_member));
SELECT setval(pg_get_serial_sequence('patient', 'id'), (SELECT MAX(id) FROM patient));
SELECT setval(pg_get_serial_sequence('service_catalog_item', 'id'), (SELECT MAX(id) FROM service_catalog_item));
SELECT setval(pg_get_serial_sequence('work_schedule_slot', 'id'), (SELECT MAX(id) FROM work_schedule_slot));
SELECT setval(pg_get_serial_sequence('blocker', 'id'), (SELECT MAX(id) FROM blocker));
SELECT setval(pg_get_serial_sequence('appointment', 'id'), (SELECT MAX(id) FROM appointment));
SELECT setval(pg_get_serial_sequence('notification', 'id'), (SELECT MAX(id) FROM notification));
SELECT setval(pg_get_serial_sequence('audit_log', 'id'), (SELECT MAX(id) FROM audit_log));
