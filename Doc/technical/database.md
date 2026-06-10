# Database Management - DentFlow

## Overview

DentFlow uses PostgreSQL 15 database with Flyway migrations.
This documentation covers backup, restore and database testing.

## Database Structure

### Schema (16 tables)

| Table | Description | Primary Key |
|-------|-------------|-------------|
| `user` | User accounts | id (BIGSERIAL) |
| `user_role` | Role assignments | user_id + role |
| `tenant` | Clinics | id (BIGSERIAL) |
| `location` | Locations | id (BIGSERIAL) |
| `room` | Treatment rooms | id (BIGSERIAL) |
| `staff_member` | Staff members | id (BIGSERIAL) |
| `staff_working_hours` | Per-day working hours | id (BIGSERIAL) |
| `staff_room` | Staff-room assignments | id (BIGSERIAL) |
| `patient` | Patients | id (BIGSERIAL) |
| `service_catalog_item` | Service catalog | id (BIGSERIAL) |
| `work_schedule_slot` | Work schedules | id (BIGSERIAL) |
| `blocker` | Time blockers | id (BIGSERIAL) |
| `appointment` | Appointments | id (BIGSERIAL) |
| `notification` | Notifications | id (BIGSERIAL) |
| `file_metadata` | File metadata | id (BIGSERIAL) |
| `audit_log` | Change audit trail | id (BIGSERIAL) |

### Relationships

```
tenant --+-- location -- room
         |
         +-- staff_member
         |     +-- work_schedule_slot
         |     +-- staff_working_hours
         |     +-- staff_room -- room
         |
         +-- patient
         |
         +-- service_catalog_item
         |
         +-- appointment --+-- staff_member (dentist)
         |                 +-- patient
         |                 +-- service_catalog_item
         |
         +-- notification -- user
         |
         +-- file_metadata
         |
         +-- audit_log -- user
```

## Migrations (Flyway)

### Identity Service
- `V1__init_identity_schema.sql` - user, user_role tables
- `V2__add_user_profile_fields.sql` - Additional profile fields
- `V3__add_user_avatar_url.sql` - Avatar field

### Core Service
- `V1__init_clinic_schema.sql` - Main schema (12 tables)
- `V3__add_logo_url.sql` - Clinic logo
- `V4__add_patient_date_of_birth.sql` - Patient date of birth
- `V5__add_staff_bio.sql` - Staff bio
- `V6__split_staff_name.sql` - Split first and last name
- `V7__add_patient_advanced_fields.sql` - Advanced patient fields
- `V8__add_staff_phone_email.sql` - Staff phone and email
- `V9__add_staff_working_hours_per_day.sql` - Per-day working hours
- `V10-V15` - Further schema evolution
- `V13__add_staff_working_hours_per_day.sql` - Per-day working hours table
- `V14__add_staff_room_assignment_and_room_crud.sql` - Staff-room assignments
- `V15__add_appointment_conflict_index.sql` - Composite index on (tenant_id, dentist_staff_id, status, start_at, end_at)

## Backup

### Using Scripts

```bash
cd DB/scripts
./backup.sh [backup_name]

# Examples
./backup.sh before_migration
./backup.sh production_20260607
```

### Manual Backup

```bash
# Backup without compression
docker exec -t postgres pg_dump -U dentflow dentflow > backup.sql

# Backup with compression
docker exec -t postgres pg_dump -U dentflow dentflow | gzip > backup.sql.gz

# Schema-only backup
docker exec -t postgres pg_dump -U dentflow dentflow --schema-only > schema.sql

# Data-only backup
docker exec -t postgres pg_dump -U dentflow dentflow --data-only > data.sql
```

### Backup Specific Table

```bash
# Backup specific table
docker exec -t postgres pg_dump -U dentflow dentflow -t appointment > appointment.sql

# Backup multiple tables
docker exec -t postgres pg_dump -U dentflow dentflow -t patient -t appointment > patients_appointments.sql
```

## Restore

### Using Scripts

```bash
cd DB/scripts
./restore.sh ../backups/backup_20260607_120000.sql.gz
```

### Manual Restore

```bash
# Restore from SQL file
docker exec -i postgres psql -U dentflow dentflow < backup.sql

# Restore from compressed file
gunzip < backup.sql.gz | docker exec -i postgres psql -U dentflow dentflow
```

### Restore Specific Table

```bash
# Restore specific table
docker exec -i postgres psql -U dentflow dentflow < appointment.sql
```

## Database Initialization

### Schema (without data)

```bash
docker exec -i postgres psql -U dentflow dentflow < DB/init_schema.sql
```

### Schema with Test Data

```bash
docker exec -i postgres psql -U dentflow dentflow < DB/init_schema_with_data.sql
```

### Using Scripts

```bash
cd DB/scripts
./init-test-data.sh
```

## Database Testing

### Integration Tests (DataJpaTest)

Repository tests verify SQL query correctness:

```bash
# Run repository tests
mvn test -pl core-service/reservation -Dtest='AppointmentRepositoryTest'
```

### Test Schema

For unit tests, H2 in-memory is used with a simplified schema:

```sql
-- core-service/reservation/src/test/resources/schema.sql
CREATE TABLE IF NOT EXISTS appointment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    ...
);
```

### Test Data Validation

Test data in `DB/seed_data.sql` includes:
- 2 clinics (DentCare Kraków, SmileClinic Warszawa)
- 10 users with password hashes
- 5 staff members
- 10 patients
- 20 services with prices
- 15 schedule slots
- 3 blockers
- 24 appointments (SCHEDULED, COMPLETED, CANCELLED, NO_SHOW)
- 8 notifications
- 8 audit log entries

## Useful SQL Queries

```sql
-- Appointment count per dentist
SELECT sm.first_name, sm.last_name, COUNT(a.id)
FROM appointment a
JOIN staff_member sm ON a.dentist_staff_id = sm.id
WHERE a.tenant_id = 1
GROUP BY sm.first_name, sm.last_name;

-- Upcoming appointments
SELECT * FROM appointment
WHERE tenant_id = 1
  AND status = 'SCHEDULED'
  AND start_at > NOW()
ORDER BY start_at
LIMIT 10;

-- Monthly appointment statistics
SELECT 
    DATE_TRUNC('month', start_at) AS month,
    COUNT(*) AS total,
    COUNT(*) FILTER (WHERE status = 'COMPLETED') AS completed,
    COUNT(*) FILTER (WHERE status = 'CANCELLED') AS cancelled
FROM appointment
WHERE tenant_id = 1
GROUP BY DATE_TRUNC('month', start_at)
ORDER BY month;

-- Patients without appointments
SELECT * FROM patient
WHERE tenant_id = 1
  AND id NOT IN (SELECT DISTINCT patient_id FROM appointment WHERE tenant_id = 1);

-- Services with most appointments
SELECT sci.name, COUNT(a.id) AS visit_count
FROM service_catalog_item sci
JOIN appointment a ON a.service_item_id = sci.id
WHERE sci.tenant_id = 1
GROUP BY sci.name
ORDER BY visit_count DESC;
```

## Troubleshooting

### Table does not exist

```sql
-- Check if table exists
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' AND table_name = 'appointment';

-- Check migrations
SELECT * FROM flyway_schema_history_core ORDER BY installed_rank DESC;
```

### Migration Conflict

```bash
# Check migration status
docker exec -t postgres psql -U dentflow dentflow -c "SELECT * FROM flyway_schema_history_core WHERE success = false;"

# Force migration (careful!)
docker exec -t postgres psql -U dentflow dentflow -c "DELETE FROM flyway_schema_history_core WHERE success = false;"
```

### Disk Space Issues

```bash
# Check database size
docker exec -t postgres psql -U dentflow dentflow -c "
SELECT pg_database.datname, 
       pg_size_pretty(pg_database_size(pg_database.datname))
FROM pg_database
WHERE datname = 'dentflow';"

# Optimization
docker exec -t postgres psql -U dentflow dentflow -c "VACUUM FULL;"
```
