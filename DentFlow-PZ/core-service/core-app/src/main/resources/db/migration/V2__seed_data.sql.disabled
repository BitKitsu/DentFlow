-- ============================================================
-- Core Service: przykładowe dane (seed)
-- Wszystkie dane testowe dla systemu DentFlow
-- ============================================================

-- -----------------------------------------------
-- 1. TENANT (gabinety stomatologiczne)
-- -----------------------------------------------
INSERT INTO tenant (id, name, status, created_at) VALUES
(1, 'DentCare Kraków',    'ACTIVE', '2025-01-15 10:00:00+01'),
(2, 'SmileClinic Warszawa', 'ACTIVE', '2025-03-01 09:00:00+01');

SELECT setval(pg_get_serial_sequence('tenant', 'id'), (SELECT MAX(id) FROM tenant));

-- -----------------------------------------------
-- 2. LOCATION (lokalizacje/placówki)
-- -----------------------------------------------
INSERT INTO location (id, tenant_id, name, address_street, address_city, address_zip, address_country) VALUES
-- Tenant 1: DentCare Kraków
(1, 1, 'DentCare Centrum',      'ul. Floriańska 15',    'Kraków',   '31-019', 'Polska'),
(2, 1, 'DentCare Nowa Huta',    'os. Centrum B 1',      'Kraków',   '31-929', 'Polska'),
-- Tenant 2: SmileClinic Warszawa
(3, 2, 'SmileClinic Mokotów',   'ul. Puławska 112',     'Warszawa', '02-620', 'Polska'),
(4, 2, 'SmileClinic Wola',      'ul. Kasprzaka 25',     'Warszawa', '01-211', 'Polska');

SELECT setval(pg_get_serial_sequence('location', 'id'), (SELECT MAX(id) FROM location));

-- -----------------------------------------------
-- 3. ROOM (gabinety/pomieszczenia)
-- -----------------------------------------------
INSERT INTO room (id, tenant_id, location_id, name) VALUES
-- DentCare Centrum (location 1)
(1, 1, 1, 'Gabinet 1 - Stomatologia zachowawcza'),
(2, 1, 1, 'Gabinet 2 - Chirurgia'),
(3, 1, 1, 'Gabinet 3 - Ortodoncja'),
-- DentCare Nowa Huta (location 2)
(4, 1, 2, 'Gabinet 1 - Ogólny'),
(5, 1, 2, 'Gabinet 2 - Protetyka'),
-- SmileClinic Mokotów (location 3)
(6, 2, 3, 'Gabinet A'),
(7, 2, 3, 'Gabinet B'),
-- SmileClinic Wola (location 4)
(8, 2, 4, 'Gabinet Główny');

SELECT setval(pg_get_serial_sequence('room', 'id'), (SELECT MAX(id) FROM room));

-- -----------------------------------------------
-- 4. STAFF_MEMBER (pracownicy)
-- -----------------------------------------------
INSERT INTO staff_member (id, tenant_id, user_id, display_name, profession) VALUES
-- Tenant 1: DentCare Kraków
(1, 1, 2, 'dr Jan Kowalski',          'DENTIST'),
(2, 1, 3, 'dr Anna Nowak',            'DENTIST'),
(3, 1, 4, 'Marek Wiśniewski',         'RECEPTIONIST'),
-- Tenant 2: SmileClinic Warszawa
(4, 2, 8, 'dr Ewa Zielińska',         'DENTIST'),
(5, 2, 9, 'Piotr Kaczmarek',          'RECEPTIONIST');

SELECT setval(pg_get_serial_sequence('staff_member', 'id'), (SELECT MAX(id) FROM staff_member));

-- -----------------------------------------------
-- 5. PATIENT (pacjenci)
-- -----------------------------------------------
INSERT INTO patient (id, tenant_id, user_id, first_name, last_name, phone, email, notes) VALUES
-- Tenant 1: DentCare
(1,  1, 5,    'Tomasz',   'Lewandowski', '+48 600 111 222', 'pacjent1@gmail.com',   'Alergia na lidokainę'),
(2,  1, 6,    'Katarzyna','Wójcik',      '+48 601 222 333', 'pacjent2@gmail.com',   NULL),
(3,  1, NULL, 'Michał',   'Kamiński',    '+48 602 333 444', 'michal.k@wp.pl',       'Stały pacjent od 2023'),
(4,  1, NULL, 'Agnieszka','Szymańska',   '+48 603 444 555', 'a.szymanska@o2.pl',    NULL),
(5,  1, NULL, 'Paweł',    'Dąbrowski',   '+48 604 555 666', NULL,                    'Pacjent z polecenia'),
(6,  1, NULL, 'Magdalena','Kozłowska',   '+48 605 666 777', 'magda.kozl@gmail.com', NULL),
-- Tenant 2: SmileClinic
(7,  2, 10,   'Robert',   'Jankowski',   '+48 700 111 222', 'pacjent3@gmail.com',   NULL),
(8,  2, NULL, 'Monika',   'Mazur',       '+48 701 222 333', 'monika.m@gmail.com',   'Bruksizm - szyna relaksacyjna'),
(9,  2, NULL, 'Krzysztof','Krawczyk',    '+48 702 333 444', 'k.krawczyk@wp.pl',     NULL),
(10, 2, NULL, 'Joanna',   'Piotrowska',  '+48 703 444 555', 'jpiotrowska@o2.pl',    'Ciąża - II trymestr');

SELECT setval(pg_get_serial_sequence('patient', 'id'), (SELECT MAX(id) FROM patient));

-- -----------------------------------------------
-- 6. SERVICE_CATALOG_ITEM (cennik usług)
-- -----------------------------------------------
INSERT INTO service_catalog_item (id, tenant_id, name, duration_minutes, price_cents, active) VALUES
-- Tenant 1: DentCare
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
-- Tenant 2: SmileClinic
(13, 2, 'Konsultacja stomatologiczna',        30,  10000, TRUE),
(14, 2, 'Higienizacja (skaling + piaskowanie)',60,  25000, TRUE),
(15, 2, 'Wypełnienie światłoutwardzalne',     45,  22000, TRUE),
(16, 2, 'Endodoncja mikroskopowa',           120,  80000, TRUE),
(17, 2, 'Implant zębowy',                    120, 500000, TRUE),
(18, 2, 'Wybielanie BEYOND',                  90, 120000, TRUE),
(19, 2, 'Ekstrakcja zęba mądrości',           60,  45000, TRUE),
(20, 2, 'Szyna relaksacyjna',                 45,  60000, TRUE);

SELECT setval(pg_get_serial_sequence('service_catalog_item', 'id'), (SELECT MAX(id) FROM service_catalog_item));

-- -----------------------------------------------
-- 7. WORK_SCHEDULE_SLOT (grafik pracy)
-- -----------------------------------------------
-- Grafik na tydzień: 2026-04-20 (poniedziałek) do 2026-04-24 (piątek)

INSERT INTO work_schedule_slot (id, tenant_id, staff_id, location_id, room_id, start_at, end_at) VALUES
-- dr Jan Kowalski (staff 1) - DentCare Centrum, Pn-Śr 8:00-16:00
(1,  1, 1, 1, 1, '2026-04-20 08:00:00+02', '2026-04-20 16:00:00+02'),
(2,  1, 1, 1, 1, '2026-04-21 08:00:00+02', '2026-04-21 16:00:00+02'),
(3,  1, 1, 1, 1, '2026-04-22 08:00:00+02', '2026-04-22 16:00:00+02'),
-- dr Jan Kowalski - DentCare Nowa Huta, Czw-Pt 10:00-18:00
(4,  1, 1, 2, 4, '2026-04-23 10:00:00+02', '2026-04-23 18:00:00+02'),
(5,  1, 1, 2, 4, '2026-04-24 10:00:00+02', '2026-04-24 18:00:00+02'),
-- dr Anna Nowak (staff 2) - DentCare Centrum, Pn-Pt 09:00-17:00
(6,  1, 2, 1, 2, '2026-04-20 09:00:00+02', '2026-04-20 17:00:00+02'),
(7,  1, 2, 1, 2, '2026-04-21 09:00:00+02', '2026-04-21 17:00:00+02'),
(8,  1, 2, 1, 2, '2026-04-22 09:00:00+02', '2026-04-22 17:00:00+02'),
(9,  1, 2, 1, 2, '2026-04-23 09:00:00+02', '2026-04-23 17:00:00+02'),
(10, 1, 2, 1, 2, '2026-04-24 09:00:00+02', '2026-04-24 17:00:00+02'),
-- dr Ewa Zielińska (staff 4) - SmileClinic Mokotów, Pn-Czw 08:00-14:00
(11, 2, 4, 3, 6, '2026-04-20 08:00:00+02', '2026-04-20 14:00:00+02'),
(12, 2, 4, 3, 6, '2026-04-21 08:00:00+02', '2026-04-21 14:00:00+02'),
(13, 2, 4, 3, 6, '2026-04-22 08:00:00+02', '2026-04-22 14:00:00+02'),
(14, 2, 4, 3, 6, '2026-04-23 08:00:00+02', '2026-04-23 14:00:00+02'),
-- dr Ewa Zielińska - SmileClinic Wola, Pt 10:00-18:00
(15, 2, 4, 4, 8, '2026-04-24 10:00:00+02', '2026-04-24 18:00:00+02');

SELECT setval(pg_get_serial_sequence('work_schedule_slot', 'id'), (SELECT MAX(id) FROM work_schedule_slot));

-- -----------------------------------------------
-- 8. BLOCKER (blokady: urlopy, niedostępność)
-- -----------------------------------------------
INSERT INTO blocker (id, tenant_id, staff_id, room_id, start_at, end_at, reason) VALUES
-- dr Kowalski - przerwa na szkolenie (środa po południu)
(1, 1, 1, NULL, '2026-04-22 13:00:00+02', '2026-04-22 16:00:00+02', 'Szkolenie - nowe materiały kompozytowe'),
-- Gabinet 2 DentCare - serwis urządzeń (piątek rano)
(2, 1, NULL, 2,   '2026-04-24 08:00:00+02', '2026-04-24 12:00:00+02', 'Przegląd techniczny unitów'),
-- dr Zielińska - wizyta lekarska
(3, 2, 4, NULL,   '2026-04-23 12:00:00+02', '2026-04-23 14:00:00+02', 'Wizyta lekarska');

SELECT setval(pg_get_serial_sequence('blocker', 'id'), (SELECT MAX(id) FROM blocker));

-- -----------------------------------------------
-- 9. APPOINTMENT (wizyty)
-- -----------------------------------------------
INSERT INTO appointment (id, tenant_id, location_id, room_id, dentist_staff_id, patient_id, service_item_id, start_at, end_at, status, created_by_user_id, created_at, updated_at, notes) VALUES
-- === Tenant 1: DentCare - Poniedziałek 20.04 ===
(1,  1, 1, 1, 1, 1, 1,  '2026-04-20 08:00:00+02', '2026-04-20 08:30:00+02', 'COMPLETED',  4, '2026-04-15 10:00:00+02', '2026-04-20 08:35:00+02', 'Przegląd ok, zalecany skaling'),
(2,  1, 1, 1, 1, 2, 3,  '2026-04-20 09:00:00+02', '2026-04-20 10:00:00+02', 'COMPLETED',  4, '2026-04-16 14:00:00+02', '2026-04-20 10:05:00+02', 'Wypełnienie ząb 36, kompozyt A3'),
(3,  1, 1, 1, 1, 3, 2,  '2026-04-20 10:30:00+02', '2026-04-20 11:15:00+02', 'COMPLETED',  4, '2026-04-17 09:00:00+02', '2026-04-20 11:20:00+02', NULL),
(4,  1, 1, 2, 2, 4, 4,  '2026-04-20 09:00:00+02', '2026-04-20 10:30:00+02', 'COMPLETED',  4, '2026-04-14 11:00:00+02', '2026-04-20 10:35:00+02', 'Kanał 1 - ząb 46. Do kontynuacji'),
(5,  1, 1, 2, 2, 5, 1,  '2026-04-20 11:00:00+02', '2026-04-20 11:30:00+02', 'NO_SHOW',    4, '2026-04-18 16:00:00+02', '2026-04-20 11:45:00+02', 'Pacjent nie stawił się'),

-- === Tenant 1: DentCare - Wtorek 21.04 ===
(6,  1, 1, 1, 1, 6, 6,  '2026-04-21 08:00:00+02', '2026-04-21 09:00:00+02', 'SCHEDULED',  4, '2026-04-19 10:00:00+02', '2026-04-19 10:00:00+02', 'Wybielanie - pierwsza wizyta'),
(7,  1, 1, 1, 1, 1, 2,  '2026-04-21 09:30:00+02', '2026-04-21 10:15:00+02', 'SCHEDULED',  4, '2026-04-19 11:00:00+02', '2026-04-19 11:00:00+02', NULL),
(8,  1, 1, 2, 2, 3, 11, '2026-04-21 09:00:00+02', '2026-04-21 10:30:00+02', 'SCHEDULED',  4, '2026-04-18 09:00:00+02', '2026-04-18 09:00:00+02', 'Korona na ząb 14 - przymiarka'),
(9,  1, 1, 2, 2, 4, 4,  '2026-04-21 11:00:00+02', '2026-04-21 12:30:00+02', 'SCHEDULED',  4, '2026-04-20 10:40:00+02', '2026-04-20 10:40:00+02', 'Kontynuacja leczenia kanałowego 46'),

-- === Tenant 1: DentCare - Środa 22.04 ===
(10, 1, 1, 1, 1, 2, 10, '2026-04-22 08:00:00+02', '2026-04-22 08:15:00+02', 'SCHEDULED',  4, '2026-04-20 14:00:00+02', '2026-04-20 14:00:00+02', 'RTG kontrolne'),
(11, 1, 1, 1, 1, 5, 1,  '2026-04-22 08:30:00+02', '2026-04-22 09:00:00+02', 'SCHEDULED',  4, '2026-04-20 15:00:00+02', '2026-04-20 15:00:00+02', 'Ponowna wizyta po no-show'),

-- === Tenant 1: DentCare - Czwartek 23.04 (Nowa Huta) ===
(12, 1, 2, 4, 1, 3, 5,  '2026-04-23 10:00:00+02', '2026-04-23 10:45:00+02', 'SCHEDULED',  4, '2026-04-19 08:00:00+02', '2026-04-19 08:00:00+02', 'Ekstrakcja 48 (ząb mądrości)'),
(13, 1, 2, 4, 1, 6, 1,  '2026-04-23 11:00:00+02', '2026-04-23 11:30:00+02', 'SCHEDULED',  4, '2026-04-20 09:00:00+02', '2026-04-20 09:00:00+02', NULL),

-- === Tenant 1: DentCare - Piątek 24.04 (Nowa Huta) ===
(14, 1, 2, 4, 1, 1, 8,  '2026-04-24 10:00:00+02', '2026-04-24 10:30:00+02', 'SCHEDULED',  4, '2026-04-20 12:00:00+02', '2026-04-20 12:00:00+02', 'Konsultacja ortodontyczna - aparat stały?'),

-- === Tenant 1: Anulowana wizyta ===
(15, 1, 1, 1, 1, 4, 7,  '2026-04-24 14:00:00+02', '2026-04-24 15:30:00+02', 'CANCELLED',  4, '2026-04-15 16:00:00+02', '2026-04-19 09:00:00+02', 'Anulowana przez pacjentkę'),

-- === Tenant 2: SmileClinic - Poniedziałek 20.04 ===
(16, 2, 3, 6, 4, 7,  13, '2026-04-20 08:00:00+02', '2026-04-20 08:30:00+02', 'COMPLETED',  9, '2026-04-14 10:00:00+02', '2026-04-20 08:35:00+02', 'Konsultacja przed implantacją'),
(17, 2, 3, 6, 4, 8,  20, '2026-04-20 09:00:00+02', '2026-04-20 09:45:00+02', 'COMPLETED',  9, '2026-04-15 11:00:00+02', '2026-04-20 09:50:00+02', 'Wycisk pod szynę relaksacyjną'),
(18, 2, 3, 6, 4, 9,  14, '2026-04-20 10:00:00+02', '2026-04-20 11:00:00+02', 'COMPLETED',  9, '2026-04-16 09:00:00+02', '2026-04-20 11:05:00+02', NULL),

-- === Tenant 2: SmileClinic - Wtorek 21.04 ===
(19, 2, 3, 6, 4, 10, 13, '2026-04-21 08:00:00+02', '2026-04-21 08:30:00+02', 'SCHEDULED',  9, '2026-04-19 14:00:00+02', '2026-04-19 14:00:00+02', 'Konsultacja - ciąża, tylko bezpieczne zabiegi'),
(20, 2, 3, 6, 4, 7,  17, '2026-04-21 09:00:00+02', '2026-04-21 11:00:00+02', 'SCHEDULED',  9, '2026-04-20 08:40:00+02', '2026-04-20 08:40:00+02', 'Implant 36 - etap I'),

-- === Tenant 2: SmileClinic - Środa 22.04 ===
(21, 2, 3, 6, 4, 8,  20, '2026-04-22 08:00:00+02', '2026-04-22 08:45:00+02', 'SCHEDULED',  9, '2026-04-20 09:55:00+02', '2026-04-20 09:55:00+02', 'Oddanie szyny relaksacyjnej'),
(22, 2, 3, 6, 4, 9,  15, '2026-04-22 09:00:00+02', '2026-04-22 09:45:00+02', 'SCHEDULED',  9, '2026-04-18 10:00:00+02', '2026-04-18 10:00:00+02', 'Wypełnienie 25'),

-- === Tenant 2: SmileClinic - Piątek 24.04 (Wola) ===
(23, 2, 4, 8, 4, 7,  18, '2026-04-24 10:00:00+02', '2026-04-24 11:30:00+02', 'SCHEDULED',  9, '2026-04-20 08:45:00+02', '2026-04-20 08:45:00+02', 'Wybielanie BEYOND'),
(24, 2, 4, 8, 4, 10, 13, '2026-04-24 12:00:00+02', '2026-04-24 12:30:00+02', 'SCHEDULED',  9, '2026-04-19 15:00:00+02', '2026-04-19 15:00:00+02', 'Kontrola - ciąża');

SELECT setval(pg_get_serial_sequence('appointment', 'id'), (SELECT MAX(id) FROM appointment));

-- -----------------------------------------------
-- 10. NOTIFICATION (powiadomienia)
-- -----------------------------------------------
INSERT INTO notification (id, tenant_id, user_id, type, message, read, created_at) VALUES
-- Tenant 1
(1, 1, 5,  'APPOINTMENT_REMINDER', 'Przypomnienie: wizyta jutro 21.04 o 09:30 - Skaling u dr. Kowalskiego', FALSE, '2026-04-20 18:00:00+02'),
(2, 1, 6,  'APPOINTMENT_REMINDER', 'Przypomnienie: wizyta jutro 21.04 o 08:00 - Wybielanie zębów u dr. Kowalskiego', FALSE, '2026-04-20 18:00:00+02'),
(3, 1, 5,  'APPOINTMENT_CONFIRMED','Twoja wizyta 20.04 o 08:00 została potwierdzona', TRUE, '2026-04-15 10:05:00+02'),
(4, 1, 2,  'SCHEDULE_CHANGE',      'Blokada: szkolenie 22.04 13:00-16:00', TRUE, '2026-04-18 09:00:00+02'),
(5, 1, 4,  'APPOINTMENT_NO_SHOW',  'Pacjent Paweł Dąbrowski nie stawił się na wizytę 20.04 11:00', FALSE, '2026-04-20 11:45:00+02'),
-- Tenant 2
(6, 2, 10, 'APPOINTMENT_REMINDER', 'Przypomnienie: wizyta jutro 21.04 o 08:00 - Konsultacja w SmileClinic', FALSE, '2026-04-20 18:00:00+02'),
(7, 2, 10, 'APPOINTMENT_CONFIRMED','Twoja wizyta 24.04 o 12:00 została potwierdzona', TRUE, '2026-04-19 15:05:00+02'),
(8, 2, 8,  'APPOINTMENT_REMINDER', 'Przypomnienie: wizyta 22.04 o 08:00 - Odbiór szyny relaksacyjnej', FALSE, '2026-04-21 18:00:00+02');

SELECT setval(pg_get_serial_sequence('notification', 'id'), (SELECT MAX(id) FROM notification));

-- -----------------------------------------------
-- 11. AUDIT_LOG (log zmian - przykładowe wpisy)
-- -----------------------------------------------
INSERT INTO audit_log (id, tenant_id, entity_name, entity_id, action, performed_by, timestamp, details) VALUES
(1, 1, 'PATIENT',     1, 'CREATE', 4, '2026-04-10 10:00:00+02', '{"firstName":"Tomasz","lastName":"Lewandowski"}'::jsonb),
(2, 1, 'PATIENT',     2, 'CREATE', 4, '2026-04-10 10:30:00+02', '{"firstName":"Katarzyna","lastName":"Wójcik"}'::jsonb),
(3, 1, 'APPOINTMENT', 1, 'CREATE', 4, '2026-04-15 10:00:00+02', '{"patientId":1,"dentistStaffId":1,"serviceItemId":1}'::jsonb),
(4, 1, 'APPOINTMENT', 1, 'UPDATE', 2, '2026-04-20 08:35:00+02', '{"status":"COMPLETED","previousStatus":"SCHEDULED"}'::jsonb),
(5, 1, 'APPOINTMENT', 5, 'UPDATE', 4, '2026-04-20 11:45:00+02', '{"status":"NO_SHOW","previousStatus":"SCHEDULED"}'::jsonb),
(6, 1, 'APPOINTMENT',15, 'UPDATE', 4, '2026-04-19 09:00:00+02', '{"status":"CANCELLED","previousStatus":"SCHEDULED"}'::jsonb),
(7, 2, 'PATIENT',     7, 'CREATE', 9, '2026-04-12 09:00:00+02', '{"firstName":"Robert","lastName":"Jankowski"}'::jsonb),
(8, 2, 'APPOINTMENT',16, 'UPDATE', 8, '2026-04-20 08:35:00+02', '{"status":"COMPLETED","previousStatus":"SCHEDULED"}'::jsonb);

SELECT setval(pg_get_serial_sequence('audit_log', 'id'), (SELECT MAX(id) FROM audit_log));
