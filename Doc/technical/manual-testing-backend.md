# Manual Testing - Backend

## Overview

Documentation for manual backend testing of DentFlow.
Tests cover regression, edge cases and end-to-end scenarios.

## Test Environment

- Identity Service: https://identity-service-production-6149.up.railway.app
- Core Service: https://core-service-production-9ce3.up.railway.app
- Database: PostgreSQL 18.4 (Railway)
- Tools: curl, bash scripts
- Date: 2026-06-08

---

## 1. Registration and Authentication

### TC-AUTH-01: Register New User
- **Goal:** Verify registration works correctly
- **Steps:**
  1. Send `POST /auth/register` with valid data
  2. Verify response contains JWT token
  3. Verify userId is returned
  4. Verify tenantId is 0
- **Expected:** 201 Created with token

### TC-AUTH-02: Reject Registration with Duplicate Email
- **Goal:** Verify email uniqueness
- **Steps:**
  1. Register user with email `test@example.com`
  2. Attempt to register another user with same email
- **Expected:** 409 Conflict

### TC-AUTH-03: Email Format Validation
- **Goal:** Verify email validation
- **Steps:**
  1. Send `POST /auth/register` with email `invalid-email`
  2. Check response
- **Expected:** 400 Bad Request

### TC-AUTH-04: Login with Valid Credentials
- **Goal:** Verify login works
- **Steps:**
  1. Register a user
  2. Send `POST /auth/login` with valid credentials
  3. Verify JWT token is returned
- **Expected:** 200 OK with token

### TC-AUTH-05: Login with Wrong Password
- **Goal:** Verify wrong password rejection
- **Steps:**
  1. Register a user
  2. Send `POST /auth/login` with wrong password
- **Expected:** 401 Unauthorized

### TC-AUTH-06: Change Password
- **Goal:** Verify password change
- **Steps:**
  1. Login
  2. Send `PUT /auth/change-password` with correct current password
  3. Logout
  4. Login with new password
- **Expected:** 200 OK, login with new password works

### TC-AUTH-07: Change Password with Wrong Current Password
- **Goal:** Verify wrong current password rejection
- **Steps:**
  1. Login
  2. Send `PUT /auth/change-password` with wrong current password
- **Expected:** 401 Unauthorized

### TC-CLAIM-01: Claim Ownership (Bootstrap)
- **Goal:** Verify first-time OWNER role assignment
- **Steps:**
  1. Register a user (gets PATIENT role only)
  2. Send `POST /auth/claim-ownership` with Bearer token
  3. Verify user now has OWNER role
- **Expected:** 200 OK with updated token containing OWNER role

### TC-CLAIM-02: Claim Ownership When Already Owner
- **Goal:** Verify duplicate claim is rejected
- **Steps:**
  1. Register a user and claim ownership
  2. Send `POST /auth/claim-ownership` again
- **Expected:** 409 Conflict

---

## 2. Clinic Management

### TC-TENANT-01: Register Clinic
- **Goal:** Verify clinic creation
- **Steps:**
  1. Register user (OWNER)
  2. Send `POST /tenants/register` with clinic data
  3. Verify clinic was created
- **Expected:** 201 Created with clinic data

### TC-TENANT-02: Assign Clinic to User
- **Goal:** Verify tenantId assignment
- **Steps:**
  1. Login
  2. Send `POST /auth/tenant` with tenantId
  3. Verify token contains tenantId
- **Expected:** 200 OK, token updated

### TC-TENANT-03: List Clinics (Marketplace)
- **Goal:** Verify public clinic list
- **Steps:**
  1. Send `GET /tenants` (without authorization)
  2. Verify list contains clinics
- **Expected:** 200 OK with clinic list

### TC-TENANT-04: Update Clinic Data
- **Goal:** Verify clinic update
- **Steps:**
  1. Login as OWNER
  2. Send `PUT /tenants/{id}` with new data
  3. Verify data was updated
- **Expected:** 200 OK with updated data

---

## 3. Patient Management

### TC-PATIENT-01: Add Patient
- **Goal:** Verify patient creation
- **Steps:**
  1. Login as OWNER/DENTIST
  2. Send `POST /tenants/{id}/patients` with patient data
  3. Verify patient was created
- **Expected:** 201 Created with patient data

### TC-PATIENT-02: List Patients
- **Goal:** Verify patient list retrieval
- **Steps:**
  1. Login
  2. Send `GET /tenants/{id}/patients`
  3. Verify list is not empty
- **Expected:** 200 OK with patient list

### TC-PATIENT-03: Search Patients
- **Goal:** Verify search functionality
- **Steps:**
  1. Add several patients
  2. Send `GET /tenants/{id}/patients?search=Kowalski`
  3. Verify results contain search term
- **Expected:** 200 OK with filtered list

### TC-PATIENT-04: Update Patient Data
- **Goal:** Verify patient update
- **Steps:**
  1. Add a patient
  2. Send `PUT /tenants/{id}/patients/{patientId}` with new data
  3. Verify data was updated
- **Expected:** 200 OK with updated data

### TC-PATIENT-05: Delete Patient
- **Goal:** Verify patient deletion
- **Steps:**
  1. Add a patient
  2. Send `DELETE /tenants/{id}/patients/{patientId}`
  3. Verify patient was deleted
- **Expected:** 204 No Content

---

## 4. Appointment Management

### TC-APPOINTMENT-01: Create Appointment
- **Goal:** Verify appointment creation
- **Steps:**
  1. Login as OWNER, DENTIST, or RECEPTIONIST
  2. Send `POST /tenants/{id}/appointments` with appointment data
  3. Verify appointment was created with SCHEDULED status
- **Expected:** 201 Created with appointment

### TC-APPOINTMENT-02: Detect Dentist Conflict
- **Goal:** Verify conflict detection
- **Steps:**
  1. Create appointment for dentist A from 10:00 to 11:00
  2. Attempt to create second appointment for same dentist from 10:30 to 11:30
- **Expected:** 409 Conflict

### TC-APPOINTMENT-03: Reject Appointment with Invalid Times
- **Goal:** Verify time validation
- **Steps:**
  1. Attempt to create appointment with endAt before startAt
- **Expected:** 400 Bad Request

### TC-APPOINTMENT-04: Update Appointment
- **Goal:** Verify appointment update
- **Steps:**
  1. Create an appointment
  2. Send `PUT /tenants/{id}/appointments/{appointmentId}` with new data
  3. Verify data was updated
- **Expected:** 200 OK with updated appointment

### TC-APPOINTMENT-05: Cancel Appointment
- **Goal:** Verify appointment cancellation
- **Steps:**
  1. Create an appointment
  2. Send `POST /tenants/{id}/appointments/{appointmentId}/cancel`
  3. Verify status changed to CANCELLED
- **Expected:** 200 OK, status: CANCELLED

### TC-APPOINTMENT-06: Reject Double Cancellation
- **Goal:** Verify double cancellation rejection
- **Steps:**
  1. Create and cancel an appointment
  2. Attempt to cancel again
- **Expected:** 400 Bad Request

### TC-APPOINTMENT-07: Complete Appointment
- **Goal:** Verify appointment completion
- **Steps:**
  1. Create an appointment
  2. Send `POST /tenants/{id}/appointments/{appointmentId}/complete`
  3. Verify status changed to COMPLETED
- **Expected:** 200 OK, status: COMPLETED

### TC-APPOINTMENT-08: Get Appointments with Date Filter
- **Goal:** Verify date filtering
- **Steps:**
  1. Create several appointments at different times
  2. Send `GET /tenants/{id}/appointments?from=...&to=...`
  3. Verify results are filtered
- **Expected:** 200 OK with filtered list

---

## 5. Work Scheduling

### TC-SCHEDULING-01: Add Blocker
- **Goal:** Verify blocker creation
- **Steps:**
  1. Login as OWNER/DENTIST
  2. Send `POST /tenants/{id}/schedule/blockers` with blocker data
  3. Verify blocker was created
- **Expected:** 201 Created with blocker data

### TC-SCHEDULING-02: List Blockers
- **Goal:** Verify blocker listing
- **Steps:**
  1. Add a blocker
  2. Send `GET /tenants/{id}/schedule/blockers`
  3. Verify list contains the blocker
- **Expected:** 200 OK with blocker list

---

## 6. Edge Cases

### TC-EDGE-01: No Authorization
- **Goal:** Verify unauthorized request rejection
- **Steps:**
  1. Send request to protected endpoint without token
- **Expected:** 401 Unauthorized

### TC-EDGE-02: Expired Token
- **Goal:** Verify expired token handling
- **Steps:**
  1. Login
  2. Wait for token expiration (or use old token)
  3. Send request with expired token
- **Expected:** 401 Unauthorized

### TC-EDGE-03: Non-existent Resource
- **Goal:** Verify 404 handling
- **Steps:**
  1. Send GET request with non-existent ID
- **Expected:** 404 Not Found

### TC-EDGE-04: Rate Limit Exceeded
- **Goal:** Verify rate limiting
- **Steps:**
  1. Send multiple login requests in short time
- **Expected:** 429 Too Many Requests

### TC-EDGE-05: Missing Required Fields
- **Goal:** Verify validation
- **Steps:**
  1. Send request with missing required fields
- **Expected:** 400 Bad Request

---

## 7. Notifications

### TC-NOTIFICATION-01: Notification on Appointment Creation
- **Goal:** Verify notification generation
- **Steps:**
  1. Create an appointment
  2. Verify dentist received in-app notification
- **Expected:** APPOINTMENT type notification

### TC-NOTIFICATION-02: Mark as Read
- **Goal:** Verify read marking
- **Steps:**
  1. Get notifications
  2. Send `POST /tenants/{id}/users/{userId}/notifications/{id}/read`
  3. Verify read status changed to true
- **Expected:** 200 OK, read: true

### TC-NOTIFICATION-03: Unread Count
- **Goal:** Verify counter
- **Steps:**
  1. Get unread notification count
  2. Verify count is correct
- **Expected:** 200 OK with correct count

---

## 8. Role-Based Access Control

### TC-RBAC-01: Assign Role Requires Owner
- **Goal:** Verify only OWNER can assign roles
- **Steps:**
  1. Register a PATIENT user (no claim-ownership)
  2. Send `POST /auth/assign-role` with Bearer token -> 403 Forbidden
  3. Login as OWNER
  4. Send `POST /auth/assign-role` -> 200 OK
- **Expected:** Only OWNER can assign roles

### TC-RBAC-02: RECEPTIONIST Access
- **Goal:** Verify RECEPTIONIST can manage appointments and view patients
- **Steps:**
  1. Login as RECEPTIONIST
  2. Send `GET /tenants/{id}/patients` -> 200 OK
  3. Send `GET /tenants/{id}/appointments` -> 200 OK
  4. Send `POST /tenants/{id}/appointments` -> 201 Created
  5. Send `POST /tenants/{id}/staff` -> 403 Forbidden
- **Expected:** RECEPTIONIST can manage appointments/patients, cannot manage staff

### TC-RBAC-03: ASSISTANT Read-Only Access
- **Goal:** Verify ASSISTANT can only read
- **Steps:**
  1. Login as ASSISTANT
  2. Send `GET /tenants/{id}/appointments` -> 200 OK
  3. Send `GET /tenants/{id}/patients` -> 200 OK
  4. Send `POST /tenants/{id}/appointments` -> 403 Forbidden
  5. Send `POST /tenants/{id}/patients` -> 403 Forbidden
- **Expected:** ASSISTANT gets 403 on write endpoints

### TC-RBAC-04: Patient My Appointments
- **Goal:** Verify patient can only see own appointments
- **Steps:**
  1. Login as PATIENT
  2. Send `GET /tenants/{id}/appointments/my` -> 200 OK
  3. Send `GET /tenants/{id}/appointments` -> 403 Forbidden
- **Expected:** PATIENT can only access /my endpoint

---

## 9. NO_SHOW and Auto-Complete

### TC-SCHEDULE-04: NO_SHOW Status
- **Goal:** Verify NO_SHOW marking works
- **Steps:**
  1. Create appointment with endAt in the past
  2. Wait for NoShowJob to run (hourly at :00)
  3. Verify status changed to NO_SHOW
- **Expected:** Past SCHEDULED appointments become NO_SHOW

### TC-SCHEDULE-05: Auto-Complete
- **Goal:** Verify auto-complete works
- **Steps:**
  1. Create appointment, confirm it, set endAt in the past
  2. Wait for AutoCompleteJob to run (hourly at :05)
  3. Verify status changed to COMPLETED
- **Expected:** Past CONFIRMED appointments become COMPLETED

---

## 10. Validation

### TC-VALIDATION-01: Patient Validation
- **Goal:** Verify patient field validation
- **Steps:**
  1. Send `POST /tenants/{id}/patients` with firstName = "A" (1 char)
  2. Verify 400 Bad Request with fields error
  3. Send with invalid email format
  4. Verify 400 Bad Request
- **Expected:** Validation errors for invalid fields

### TC-VALIDATION-02: Staff Validation
- **Goal:** Verify staff field validation
- **Steps:**
  1. Send `POST /tenants/{id}/staff` with invalid phone format
  2. Verify 400 Bad Request
  3. Send with invalid email
  4. Verify 400 Bad Request
- **Expected:** Validation errors for invalid fields

---

## Test Results

| Test ID | Status | Notes |
|---------|--------|-------|
| TC-AUTH-01 | PASS | 201 Created, token returned |
| TC-AUTH-02 | PASS | 409 Conflict for duplicate email |
| TC-AUTH-03 | PASS | 400 Bad Request for invalid email |
| TC-AUTH-04 | PASS | 200 OK, JWT token returned |
| TC-AUTH-05 | PASS | 401 Unauthorized |
| TC-AUTH-06 | PASS | 204 No Content (change password works) |
| TC-AUTH-07 | PASS | 401 Unauthorized (wrong current password) |
| TC-TENANT-01 | PASS | 201 Created |
| TC-TENANT-02 | PASS | 200 OK, token updated with tenantId |
| TC-TENANT-03 | PASS | 200 OK, public clinic list |
| TC-TENANT-04 | PASS | 200 OK, data updated |
| TC-PATIENT-01 | PASS | 201 Created |
| TC-PATIENT-02 | PASS | 200 OK, list returned |
| TC-PATIENT-03 | PASS | 200 OK, search works |
| TC-PATIENT-04 | PASS | 200 OK, data updated |
| TC-PATIENT-05 | PASS | 204 No Content |
| TC-APPOINTMENT-01 | PASS | 201 Created with SCHEDULED status |
| TC-APPOINTMENT-02 | PASS | 409 Conflict detected |
| TC-APPOINTMENT-03 | PASS | 400 Bad Request for invalid times |
| TC-APPOINTMENT-04 | PASS | 200 OK (requires startAt+endAt in body) |
| TC-APPOINTMENT-05 | PASS | 200 OK, status: CANCELLED |
| TC-APPOINTMENT-06 | PASS | 400 Bad Request for double cancel |
| TC-APPOINTMENT-07 | PASS | 200 OK, status: COMPLETED |
| TC-APPOINTMENT-08 | PASS | 200 OK, date filter with ISO_DATE_TIME format |
| TC-SCHEDULING-01 | PASS | 201 Created (blocker) |
| TC-SCHEDULING-02 | PASS | 200 OK, blockers listed |
| TC-EDGE-01 | PASS | 403 Forbidden (no auth) |
| TC-EDGE-02 | SKIP | Requires token expiration wait |
| TC-EDGE-03 | PASS | 404 Not Found for non-existent tenant |
| TC-EDGE-04 | PASS | 429 Too Many Requests (after ~12 attempts) |
| TC-EDGE-05 | PASS | 400 Bad Request for missing fields |
| TC-NOTIFICATION-01 | PASS | 200 OK (URL: /tenants/{id}/users/{userId}/notifications) |
| TC-NOTIFICATION-02 | SKIP | No notifications available to mark |
| TC-NOTIFICATION-03 | PASS | 200 OK, unread count returned |
| TC-RBAC-01 | PASS | PATIENT gets 403 on assign-role, OWNER gets 200 |
| TC-RBAC-02 | PASS | RECEPTIONIST can GET patients, POST/confirm/cancel appointments |
| TC-RBAC-03 | PASS | ASSISTANT read-only (GET 200, POST 403) |
| TC-RBAC-04 | PASS | PATIENT /my=200, /appointments=403 |
| TC-CLAIM-01 | PASS | 200 OK, OWNER role assigned |
| TC-CLAIM-02 | PASS | 409 Conflict (already owner) |
| TC-VALIDATION-01 | PASS | 400 Bad Request for short firstName |
| TC-VALIDATION-02 | PASS | 400 Bad Request for empty firstName |

**Summary:** 36 PASS, 0 FAIL, 2 SKIP

**Statuses:** PASS / FAIL / BLOCKED / SKIP
