# Manual Testing - Android Mobile App

## Overview

Documentation for manual testing of the DentFlow Android mobile application.

## Test Environment

- Application: DentFlow Android
- OS: Android 8.0+ (API 26+)
- Devices: Phone and tablet
- Backend: DentFlow Backend (port 8080, 8081)

---

## 1. Login and Registration

### TC-MOBILE-01: Login Screen
- **Goal:** Verify login screen display
- **Steps:**
  1. Launch the application
  2. Verify email and password fields are displayed
  3. Verify "Login" button is visible
  4. Verify "Registration" link is visible
- **Expected:** Form displays correctly

### TC-MOBILE-02: Login with Valid Credentials
- **Goal:** Verify login works
- **Steps:**
  1. Enter valid email
  2. Enter valid password
  3. Tap "Login"
  4. Verify navigation to main screen
- **Expected:** Navigation to dashboard

### TC-MOBILE-03: Login with Invalid Credentials
- **Goal:** Verify error handling
- **Steps:**
  1. Enter invalid email or password
  2. Tap "Login"
  3. Verify error message is displayed
- **Expected:** Error message for wrong email/password

### TC-MOBILE-04: Register New User
- **Goal:** Verify registration
- **Steps:**
  1. Tap "Registration"
  2. Fill registration form
  3. Tap "Register"
  4. Verify navigation to login screen
- **Expected:** Navigation to login screen

### TC-MOBILE-05: Registration Form Validation
- **Goal:** Verify field validation
- **Steps:**
  1. Attempt to register with empty fields
  2. Attempt to register with invalid email
  3. Attempt to register with short password
- **Expected:** Validation error messages

---

## 2. Navigation and Dashboard

### TC-MOBILE-06: Bottom Navigation
- **Goal:** Verify navigation
- **Steps:**
  1. Tap each tab on navigation bar
  2. Verify navigation to correct screen
- **Expected:** Correct navigation between screens

### TC-MOBILE-07: Dashboard Screen
- **Goal:** Verify dashboard display
- **Steps:**
  1. Login
  2. Verify statistics are displayed
  3. Verify upcoming appointments are displayed
- **Expected:** Data displays correctly

### TC-MOBILE-08: Logout
- **Goal:** Verify logout
- **Steps:**
  1. Navigate to "Account" tab
  2. Tap "Logout"
  3. Verify navigation to login screen
- **Expected:** Navigation to login screen

---

## 3. Clinic Management

### TC-MOBILE-09: Clinic List (Marketplace)
- **Goal:** Verify clinic list display
- **Steps:**
  1. Login
  2. Navigate to "Offers" tab
  3. Verify clinics are displayed
- **Expected:** Clinic list with names and addresses

### TC-MOBILE-10: Create Clinic
- **Goal:** Verify clinic creation
- **Steps:**
  1. Tap "Create clinic"
  2. Fill form
  3. Tap "Save"
  4. Verify clinic was created
- **Expected:** New clinic on list

### TC-MOBILE-11: Manage Clinic (Owner)
- **Goal:** Verify management options
- **Steps:**
  1. Login as clinic owner
  2. Navigate to "Business" tab
  3. Verify management options are displayed
- **Expected:** Clinic management panel

---

## 4. Patients

### TC-MOBILE-12: Patient List
- **Goal:** Verify list display
- **Steps:**
  1. Login as dentist/receptionist
  2. Navigate to "Patients" tab
  3. Verify patient list is displayed
- **Expected:** Patient list with data

### TC-MOBILE-13: Search Patients
- **Goal:** Verify search
- **Steps:**
  1. Enter search term in search field
  2. Verify results are filtered
- **Expected:** Filtered list

### TC-MOBILE-14: Add Patient
- **Goal:** Verify add form
- **Steps:**
  1. Tap "Add patient"
  2. Fill form
  3. Tap "Save"
  4. Verify patient was added
- **Expected:** New patient on list

### TC-MOBILE-15: Edit Patient Data
- **Goal:** Verify edit
- **Steps:**
  1. Select patient from list
  2. Tap "Edit"
  3. Change data
  4. Tap "Save"
- **Expected:** Updated patient data

---

## 5. Appointments

### TC-MOBILE-16: Appointment List
- **Goal:** Verify list display
- **Steps:**
  1. Navigate to "Appointments" tab
  2. Verify appointments are displayed
  3. Verify appointments have statuses (SCHEDULED, COMPLETED, CANCELLED)
- **Expected:** Appointment list with statuses

### TC-MOBILE-17: Create Appointment
- **Goal:** Verify booking
- **Steps:**
  1. Tap "New appointment"
  2. Select patient
  3. Select dentist
  4. Select time
  5. Tap "Book"
  6. Verify appointment was created
- **Expected:** New appointment on list

### TC-MOBILE-18: Edit Appointment
- **Goal:** Verify edit
- **Steps:**
  1. Select appointment
  2. Tap "Edit"
  3. Change time
  4. Tap "Save"
- **Expected:** Updated appointment

### TC-MOBILE-19: Cancel Appointment
- **Goal:** Verify cancellation
- **Steps:**
  1. Select appointment
  2. Tap "Cancel"
  3. Confirm cancellation
  4. Verify status changed to CANCELLED
- **Expected:** Status: CANCELLED

### TC-MOBILE-20: Complete Appointment
- **Goal:** Verify completion marking
- **Steps:**
  1. Select appointment
  2. Tap "Complete"
  3. Verify status changed to COMPLETED
- **Expected:** Status: COMPLETED

---

## 6. Work Schedule

### TC-MOBILE-21: View Schedule
- **Goal:** Verify schedule display
- **Steps:**
  1. Navigate to "Schedule" tab
  2. Verify slots are displayed
  3. Verify slots have colors (available/unavailable)
- **Expected:** Colorful schedule with slots

### TC-MOBILE-22: Add Schedule Slot
- **Goal:** Verify slot creation
- **Steps:**
  1. Tap "Add slot"
  2. Select staff member
  3. Set working hours
  4. Tap "Save"
- **Expected:** New slot on schedule

### TC-MOBILE-23: Add Blocker
- **Goal:** Verify blocker creation
- **Steps:**
  1. Tap "Add blocker"
  2. Select reason (vacation, training, etc.)
  3. Set dates
  4. Tap "Save"
- **Expected:** Blocker on schedule

---

## 7. Notifications

### TC-MOBILE-24: Notification List
- **Goal:** Verify list display
- **Steps:**
  1. Tap notification icon
  2. Verify notifications are displayed
  3. Verify unread are marked
- **Expected:** Notification list with markings

### TC-MOBILE-25: Mark as Read
- **Goal:** Verify read marking
- **Steps:**
  1. Tap on notification
  2. Verify it was marked as read
- **Expected:** Status: read

### TC-MOBILE-26: Unread Count on Icon
- **Goal:** Verify counter
- **Steps:**
  1. Check if notification icon shows count
  2. Mark notification as read
  3. Verify count decreased
- **Expected:** Count updates correctly

---

## 8. PDF Reports

### TC-MOBILE-27: Report List
- **Goal:** Verify list display
- **Steps:**
  1. Navigate to "Reports" tab
  2. Verify available reports are displayed
- **Expected:** List of available reports

### TC-MOBILE-28: Generate Appointment Report
- **Goal:** Verify report generation
- **Steps:**
  1. Select "Appointment list"
  2. Set parameters (date range, doctor)
  3. Tap "Generate"
  4. Verify report was generated
- **Expected:** PDF report ready for download

### TC-MOBILE-29: Download Report
- **Goal:** Verify download
- **Steps:**
  1. Generate report
  2. Tap "Download"
  3. Verify file was downloaded
- **Expected:** PDF file in device memory

---

## 9. Service Catalog

### TC-MOBILE-30: Service List
- **Goal:** Verify catalog display
- **Steps:**
  1. Navigate to "Price list" tab
  2. Verify services with prices are displayed
- **Expected:** Service list with prices and duration

### TC-MOBILE-31: Add Service
- **Goal:** Verify service creation
- **Steps:**
  1. Tap "Add service"
  2. Fill form (name, price, duration)
  3. Tap "Save"
- **Expected:** New service on list

---

## 10. Mobile Edge Cases

### TC-MOBILE-32: No Internet Connection
- **Goal:** Verify offline handling
- **Steps:**
  1. Turn off internet
  2. Attempt to perform operation
  3. Verify error message is displayed
- **Expected:** No connection error message

### TC-MOBILE-33: Screen Rotation
- **Goal:** Verify responsiveness
- **Steps:**
  1. Rotate device (portrait -> landscape)
  2. Verify interface adapted
  3. Verify data was not lost
- **Expected:** Correct display after rotation

### TC-MOBILE-34: Back to Main Screen
- **Goal:** Verify back navigation
- **Steps:**
  1. Navigate through several screens
  2. Tap back button
  3. Verify navigation works correctly
- **Expected:** Correct back navigation

### TC-MOBILE-35: Long Names and Texts
- **Goal:** Verify long text display
- **Steps:**
  1. Enter long patient/service name
  2. Verify text is appropriately truncated
- **Expected:** Text is readable, not cut off

### TC-MOBILE-36: Rapid Taps
- **Goal:** Verify rapid tap handling
- **Steps:**
  1. Tap button multiple times quickly
  2. Verify application did not freeze
- **Expected:** Application responds correctly

---

## 11. Role-Based Access (Klinika Tab)

### TC-MOBILE-37: Owner Klinika Tab
- **Goal:** Verify owner sees all menu items
- **Steps:**
  1. Login as OWNER
  2. Navigate to "Klinika" tab
  3. Verify all items visible: Pracownicy, Pacjenci, Gabinety, Wizyty w klinice, Cennik uslug, Grafik pracy, Raporty PDF, Edytuj dane kliniki
- **Expected:** All 8 menu items visible

### TC-MOBILE-38: Dentist Klinika Tab
- **Goal:** Verify dentist sees limited items, no edit
- **Steps:**
  1. Login as DENTIST
  2. Navigate to "Klinika" tab
  3. Verify: Pacjenci, Gabinety, Wizyty w klinice, Cennik uslug, Grafik pracy visible
  4. Verify: Pracownicy, Raporty PDF, Edytuj dane kliniki hidden
  5. Open Gabinety -> verify no add/edit/delete buttons
  6. Open Cennik uslug -> verify no add/edit/delete buttons
  7. Open Grafik pracy -> verify no add/delete buttons
- **Expected:** Menu items visible but read-only where applicable

### TC-MOBILE-39: Receptionist Klinika Tab
- **Goal:** Verify receptionist access
- **Steps:**
  1. Login as RECEPTIONIST
  2. Navigate to "Klinika" tab
  3. Verify: Pacjenci, Gabinety, Wizyty w klinice, Cennik uslug, Grafik pracy visible
  4. Verify: Pracownicy, Raporty PDF, Edytuj dane kliniki hidden
  5. Open Gabinety -> verify can add/edit rooms
  6. Open Wizyty w klinice -> verify can confirm/cancel/complete
- **Expected:** Correct menu items with edit access where applicable

### TC-MOBILE-40: Assistant Klinika Tab (Read-Only)
- **Goal:** Verify assistant has read-only access
- **Steps:**
  1. Login as ASSISTANT
  2. Navigate to "Klinika" tab
  3. Verify same items as RECEPTIONIST
  4. Open Wizyty w klinice -> verify no action buttons (confirm/cancel/complete)
  5. Open Gabinety -> verify no add/edit/delete buttons
- **Expected:** All items read-only

### TC-MOBILE-41: Patient Cannot See Klinika Tab
- **Goal:** Verify patient role
- **Steps:**
  1. Login as PATIENT
  2. Verify "Klinika" tab is not visible in bottom navigation
- **Expected:** No Klinika tab

---

## 12. NO_SHOW Status

### TC-MOBILE-42: NO_SHOW Status Display
- **Goal:** Verify NO_SHOW status shown correctly
- **Steps:**
  1. Login as DENTIST/RECEPTIONIST
  2. Navigate to "Wizyty" tab
  3. Verify NO_SHOW appointments have purple dot
  4. Verify "Nieobecnosc" label displayed
- **Expected:** Purple dot with "Nieobecnosc" label

---

## Test Results

| Test ID | Status | Notes |
|---------|--------|-------|
| TC-MOBILE-01 | | |
| TC-MOBILE-02 | | |
| TC-MOBILE-03 | | |
| TC-MOBILE-04 | | |
| TC-MOBILE-05 | | |
| TC-MOBILE-06 | | |
| TC-MOBILE-07 | | |
| TC-MOBILE-08 | | |
| TC-MOBILE-09 | | |
| TC-MOBILE-10 | | |
| TC-MOBILE-11 | | |
| TC-MOBILE-12 | | |
| TC-MOBILE-13 | | |
| TC-MOBILE-14 | | |
| TC-MOBILE-15 | | |
| TC-MOBILE-16 | | |
| TC-MOBILE-17 | | |
| TC-MOBILE-18 | | |
| TC-MOBILE-19 | | |
| TC-MOBILE-20 | | |
| TC-MOBILE-21 | | |
| TC-MOBILE-22 | | |
| TC-MOBILE-23 | | |
| TC-MOBILE-24 | | |
| TC-MOBILE-25 | | |
| TC-MOBILE-26 | | |
| TC-MOBILE-27 | | |
| TC-MOBILE-28 | | |
| TC-MOBILE-29 | | |
| TC-MOBILE-30 | | |
| TC-MOBILE-31 | | |
| TC-MOBILE-32 | | |
| TC-MOBILE-33 | | |
| TC-MOBILE-34 | | |
| TC-MOBILE-35 | | |
| TC-MOBILE-36 | | |
| TC-MOBILE-37 | | |
| TC-MOBILE-38 | | |
| TC-MOBILE-39 | | |
| TC-MOBILE-40 | | |
| TC-MOBILE-41 | | |
| TC-MOBILE-42 | | |

**Statuses:** PASS / FAIL / BLOCKED / SKIP
