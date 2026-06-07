# DentFlow API Reference

## Overview

DentFlow exposes two REST APIs:
- **Identity Service** (port 8081) - Authentication and user management
- **Core Service** (port 8080) - Business logic

## Authentication

All protected endpoints require the header:
```
Authorization: Bearer <JWT token>
```

The token is generated after login (`POST /auth/login`).

---

## Identity Service API

### Registration

```
POST /auth/register
```

Request:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "Jan",
  "lastName": "Kowalski",
  "phone": "+48 123 456 789",
  "addressStreet": "ul. Kwiatowa 1",
  "addressCity": "Kraków",
  "addressZip": "31-001",
  "addressCountry": "Polska"
}
```

Response (201 Created):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "email": "user@example.com",
  "tenantId": 0
}
```

### Login

```
POST /auth/login
```

Request:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

Response (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "email": "user@example.com",
  "tenantId": 1
}
```

### Logout

```
POST /auth/logout
```

Response: 204 No Content

### Change Password

```
PUT /auth/change-password
```

Request:
```json
{
  "currentPassword": "oldPassword",
  "newPassword": "newPassword"
}
```

Response: 204 No Content

### Update Profile

```
PUT /auth/profile
```

Request:
```json
{
  "firstName": "Jan",
  "lastName": "Kowalski",
  "phone": "+48 123 456 789",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

Response (200 OK): AuthResponse with updated data

### Assign Clinic

```
POST /auth/tenant
```

Request:
```json
{
  "tenantId": 1
}
```

Response (200 OK): AuthResponse with assigned tenantId

### Assign Role

> Requires `OWNER` role.

```
POST /auth/assign-role
```

Request:
```json
{
  "userId": 1,
  "role": "DENTIST"
}
```

Response (200 OK): AuthResponse

### Check Email

```
GET /auth/check-email?email=user@example.com
```

Response (200 OK): `1` (userId) or 404 Not Found

---

## Core Service API

### Clinics (Tenants)

#### Register Clinic

```
POST /tenants/register
```

Request:
```json
{
  "name": "DentCare Kraków",
  "locationName": "DentCare Centrum",
  "addressStreet": "ul. Floriańska 15",
  "addressCity": "Kraków",
  "addressZip": "31-019",
  "addressCountry": "Polska"
}
```

Response (201 Created): TenantResponse

#### List Clinics

```
GET /tenants
```

Response (200 OK): `[{ id: 1, name: "DentCare Kraków", ... }]`

#### Clinic Details

```
GET /tenants/{tenantId}
```

Response (200 OK): TenantResponse

#### Update Clinic

```
PUT /tenants/{tenantId}
```

Request:
```json
{
  "name": "DentCare Kraków (updated)",
  "logoUrl": "https://example.com/logo.png"
}
```

Response (200 OK): TenantResponse

---

### Patients

#### List Patients

> Requires: OWNER, DENTIST, or ASSISTANT role.

```
GET /tenants/{tenantId}/patients?search=kowalski
```

Response (200 OK): `[{ id: 1, firstName: "Jan", lastName: "Kowalski", ... }]`

#### Patient Details

```
GET /tenants/{tenantId}/patients/{patientId}
```

Response (200 OK): PatientResponse

#### Add Patient

> Requires: OWNER or DENTIST role.

```
POST /tenants/{tenantId}/patients
```

Request:
```json
{
  "firstName": "Jan",
  "lastName": "Kowalski",
  "phone": "+48 123 456 789",
  "email": "jan@example.com",
  "dateOfBirth": "1990-01-15",
  "pesel": "90011512345",
  "gender": "MALE"
}
```

Response (201 Created): PatientResponse

#### Update Patient

> Requires: OWNER or DENTIST role.

```
PUT /tenants/{tenantId}/patients/{patientId}
```

Request: PatientRequest

Response (200 OK): PatientResponse

#### Delete Patient

> Requires: OWNER role only.

```
DELETE /tenants/{tenantId}/patients/{patientId}
```

Response: 204 No Content

---

### Appointments

#### List Appointments

```
GET /tenants/{tenantId}/appointments?from=2026-01-01T00:00:00Z&to=2026-12-31T23:59:59Z
```

Response (200 OK): `[{ id: 1, status: "SCHEDULED", startAt: "...", ... }]`

Statuses: `SCHEDULED`, `CONFIRMED`, `COMPLETED`, `CANCELLED`, `NO_SHOW`

#### My Appointments

```
GET /tenants/{tenantId}/appointments/my
```

Response (200 OK): List of logged-in user's appointments

#### Appointment Details

```
GET /tenants/{tenantId}/appointments/{appointmentId}
```

Response (200 OK): AppointmentResponse

#### Create Appointment

```
POST /tenants/{tenantId}/appointments
```

Request:
```json
{
  "locationId": 1,
  "roomId": 1,
  "dentistStaffId": 1,
  "patientId": 1,
  "serviceItemId": 3,
  "startAt": "2026-06-10T10:00:00+02:00",
  "endAt": "2026-06-10T11:00:00+02:00",
  "notes": "Dental checkup"
}
```

Response (201 Created): AppointmentResponse

#### Update Appointment

```
PUT /tenants/{tenantId}/appointments/{appointmentId}
```

Request:
```json
{
  "startAt": "2026-06-11T10:00:00+02:00",
  "endAt": "2026-06-11T11:00:00+02:00",
  "notes": "Rescheduled"
}
```

Response (200 OK): AppointmentResponse

#### Cancel Appointment

```
POST /tenants/{tenantId}/appointments/{appointmentId}/cancel
```

Response (200 OK): AppointmentResponse (status: "CANCELLED")

#### Complete Appointment

```
POST /tenants/{tenantId}/appointments/{appointmentId}/complete
```

Response (200 OK): AppointmentResponse (status: "COMPLETED")

---

### Work Scheduling

#### Schedule Slots

```
GET /tenants/{tenantId}/scheduling/slots?from=...&to=...
```

Response (200 OK): `[{ id: 1, staffId: 1, startAt: "...", ... }]`

#### Add Slot

```
POST /tenants/{tenantId}/scheduling/slots
```

Request:
```json
{
  "staffId": 1,
  "locationId": 1,
  "roomId": 1,
  "startAt": "2026-06-10T08:00:00+02:00",
  "endAt": "2026-06-10T16:00:00+02:00"
}
```

Response (201 Created): WorkScheduleSlotResponse

#### Time Blockers

```
GET /tenants/{tenantId}/scheduling/blockers
```

Response (200 OK): `[{ id: 1, reason: "Vacation", ... }]`

#### Add Blocker

```
POST /tenants/{tenantId}/scheduling/blockers
```

Request:
```json
{
  "staffId": 1,
  "startAt": "2026-06-15T08:00:00+02:00",
  "endAt": "2026-06-19T16:00:00+02:00",
  "reason": "Annual leave"
}
```

Response (201 Created): BlockerResponse

---

### Service Catalog

#### List Services

```
GET /tenants/{tenantId}/catalog
```

Response (200 OK): `[{ id: 1, name: "Checkup", durationMinutes: 30, priceCents: 15000, ... }]`

#### Add Service

```
POST /tenants/{tenantId}/catalog
```

Request:
```json
{
  "name": "Dental checkup",
  "durationMinutes": 30,
  "priceCents": 15000,
  "active": true
}
```

Response (201 Created): ServiceCatalogItemDTO

---

### Notifications

#### List Notifications

```
GET /tenants/{tenantId}/users/{userId}/notifications?unreadOnly=true
```

Response (200 OK): `[{ id: 1, type: "APPOINTMENT", message: "...", read: false, ... }]`

#### Unread Count

```
GET /tenants/{tenantId}/users/{userId}/notifications/unread-count
```

Response (200 OK): `5`

#### Mark as Read

```
POST /tenants/{tenantId}/users/{userId}/notifications/{notificationId}/read
```

Response (200 OK): NotificationResponse (read: true)

#### Mark All as Read

```
POST /tenants/{tenantId}/users/{userId}/notifications/read-all
```

Response: 204 No Content

---

### Files

#### Upload File

```
POST /tenants/{tenantId}/files
Content-Type: multipart/form-data
```

Form data:
- `file`: File (max 10MB)

Response (201 Created): FileUploadResponse

#### List Files

```
GET /tenants/{tenantId}/files
```

Response (200 OK): `[{ id: 1, originalName: "document.pdf", ... }]`

#### Download File

```
GET /tenants/{tenantId}/files/{fileId}/download
```

Response: Binary file

#### Delete File

```
DELETE /tenants/{tenantId}/files/{fileId}
```

Response: 204 No Content

---

### PDF Reports

#### Appointment List (PDF)

```
GET /tenants/{tenantId}/reports/appointment-list?from=...&to=...
```

Response: PDF file

#### Patient Visit History (PDF)

```
GET /tenants/{tenantId}/reports/patient-history/{patientId}?from=...&to=...
```

Response: PDF file

#### Room Occupancy (PDF)

```
GET /tenants/{tenantId}/reports/room-occupancy?year=2026&month=6
```

Response: PDF file

---

## Error Codes

| Code | Description |
|------|-------------|
| 400 | Bad Request - invalid data |
| 401 | Unauthorized - no authorization |
| 403 | Forbidden - no permissions |
| 404 | Not Found - resource does not exist |
| 409 | Conflict - conflict (e.g., appointment overlap) |
| 429 | Too Many Requests - rate limit |
| 500 | Internal Server Error - server error |

---

## Validation Rules

### Registration (`POST /auth/register`)
| Field | Rule |
|-------|------|
| email | Required, valid email format |
| password | Min 8 characters |

### Patient (`POST /tenants/{id}/patients`)
| Field | Rule |
|-------|------|
| firstName | Required, 2-50 chars, `^[a-zA-Z\d\s\-]+$` |
| lastName | Required, 2-50 chars, `^[a-zA-Z\d\s\-]+$` |
| phone | Optional, `^\+?[0-9][\s\-]?([0-9][\s\-]?){8,14}$` |
| email | Optional, valid email format |

### Staff Member (`POST /tenants/{id}/staff`)
| Field | Rule |
|-------|------|
| firstName | Required, 2-50 chars |
| lastName | Required, 2-50 chars |
| email | Required, valid email format |
| phone | Optional, `^\+?[0-9][\s\-]?([0-9][\s\-]?){8,14}$` |

### Location (`POST /tenants/{id}/locations`)
| Field | Rule |
|-------|------|
| locationName | Required, min 2 chars |

### Blocker (`POST /tenants/{id}/scheduling/blockers`)
| Field | Rule |
|-------|------|
| reason | Optional, max 255 chars |

### Notification (`POST /tenants/{id}/notifications`)
| Field | Rule |
|-------|------|
| type | Required, max 50 chars |
| message | Required, max 500 chars |

### Validation Error Response (400)
```json
{
  "error": "Walidacja nie powiodla sie",
  "fields": {
    "firstName": "must be between 2 and 50 characters",
    "email": "must be a valid email"
  }
}
```
