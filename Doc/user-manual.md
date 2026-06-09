# DentFlow User Manual

## Table of Contents

1. [Introduction](#introduction)
2. [Registration and Login](#registration-and-login)
3. [Clinic Management](#clinic-management)
4. [Patient Management](#patient-management)
5. [Appointment Booking](#appointment-booking)
6. [Work Schedule](#work-schedule)
7. [Service Catalog](#service-catalog)
8. [Notifications](#notifications)
9. [Reports](#reports)
10. [Account Settings](#account-settings)

---

## Introduction

DentFlow is a modern system for dental clinic management.
The application enables:

- Clinic and location management
- Patient records
- Appointment booking
- Work schedule management
- Service catalog with price list
- In-app and email notifications
- PDF report generation

### Requirements

- Mobile app: Android 8.0+
- Internet connection
- DentFlow system account

### User Roles

| Role | Description |
|------|-------------|
| OWNER | Clinic owner - full access to all features |
| DENTIST | Dentist - manage appointments and patients, view rooms/catalog/schedule |
| RECEPTIONIST | Receptionist - manage appointments and patients, manage rooms, view catalog/schedule |
| ASSISTANT | Dental assistant - read-only access to appointments, patients, rooms, catalog, schedule |
| PATIENT | Patient - view own appointments only |

---

## Registration and Login

### Register New Account

1. Open DentFlow application
2. Tap **"Registration"** on login screen
3. Fill registration form:
   - Email (required)
   - Password (min. 8 characters)
   - First and last name (optional)
   - Phone (optional)
4. Tap **"Register"**
5. Confirm registration via email (if required)

### Login

1. Open DentFlow application
2. Enter email and password
3. Tap **"Login"**
4. After successful login, you will be redirected to main screen

### Logout

1. Navigate to **"Account"** tab
2. Tap **"Logout"**
3. Confirm logout

---

## Clinic Management

### Create Clinic

1. Login as owner
2. Navigate to **"Offers"** tab
3. Tap **"Create clinic"**
4. Fill form:
   - Clinic name
   - Location name
   - Address (street, city, postal code, country)
5. Tap **"Save"**

### Edit Clinic Data

1. Navigate to **"Business"** tab
2. Tap **"Edit"** next to clinic data
3. Change required fields
4. Tap **"Save"**

### Add Location

1. Navigate to **"Business"** tab
2. Tap **"Add location"**
3. Fill form
4. Tap **"Save"**

### Manage Staff

1. Navigate to **"Business"** tab
2. Tap **"Staff"**
3. To add staff: **"Add staff member"**
4. To edit: tap on staff member and **"Edit"**
5. To delete: tap on staff member and **"Delete"**

---

## Patient Management

### Add Patient

1. Navigate to **"Patients"** tab
2. Tap **"Add patient"**
3. Fill form:
   - First and last name (required)
   - Phone
   - Email
   - Date of birth
   - PESEL
   - Gender
   - Address
   - Notes
4. Tap **"Save"**

### Edit Patient Data

1. Navigate to **"Patients"** tab
2. Select patient from list
3. Tap **"Edit"**
4. Change required fields
5. Tap **"Save"**

### Search Patients

1. Navigate to **"Patients"** tab
2. Enter search term in **"Search"** field
3. Results will display automatically

### Delete Patient

1. Select patient from list
2. Tap **"Delete"**
3. Confirm deletion

---

## Appointment Booking

### Create New Appointment

1. Navigate to **"Appointments"** tab
2. Tap **"New appointment"**
3. Select:
   - Patient
   - Dentist
   - Location and room
   - Service (optional)
   - Appointment time (date and hour)
4. Add notes (optional)
5. Tap **"Book"**

### Appointment View

Appointments can be displayed in:
- **List** - traditional appointment list
- **Calendar** - calendar view with appointments

### Filters

You can filter appointments by:
- Date range
- Dentist
- Status (SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW)

### Cancel Appointment

1. Select appointment from list
2. Tap **"Cancel"**
3. Confirm cancellation
4. Status will change to **CANCELLED**

### Complete Appointment

1. Select appointment from list
2. Tap **"Complete"**
3. Status will change to **COMPLETED**

---

## Work Schedule

### View Schedule

1. Navigate to **"Schedule"** tab
2. Schedule with work slots will be displayed
3. Colors indicate:
   - **Green** - available
   - **Red** - unavailable (blocker)
   - **Gray** - no slot

### Add Work Slot

1. Tap **"Add slot"**
2. Select staff member
3. Set:
   - Location
   - Room
   - Working hours (start and end)
4. Tap **"Save"**

### Add Blocker

1. Tap **"Add blocker"**
2. Select reason:
   - Vacation
   - Training
   - Technical inspection
   - Other
3. Set:
   - Staff member (optional)
   - Room (optional)
   - Dates and hours
4. Tap **"Save"**

### Delete Slot/Blocker

1. Tap on slot or blocker
2. Tap **"Delete"**
3. Confirm deletion

---

## Service Catalog

### View Price List

1. Navigate to **"Price list"** tab
2. Service list with prices and duration will be displayed

### Add Service

1. Tap **"Add service"**
2. Fill form:
   - Service name (required)
   - Duration (in minutes)
   - Price (in PLN)
   - Status (active/inactive)
3. Tap **"Save"**

### Edit Service

1. Select service from list
2. Tap **"Edit"**
3. Change required fields
4. Tap **"Save"**

### Delete Service

1. Select service from list
2. Tap **"Delete"**
3. Confirm deletion

---

## Notifications

### View Notifications

1. Tap **bell icon** in upper part of screen
2. Notification list will be displayed
3. Unread notifications have a dot

### Mark as Read

1. Tap on notification
2. It will be marked as read

### Mark All as Read

1. Tap **"Mark all as read"**
2. All notifications will be marked

### Notification Types

- **APPOINTMENT** - new appointment created
- **APPOINTMENT_CANCELLED** - appointment cancelled
- **APPOINTMENT_COMPLETED** - appointment completed
- **APPOINTMENT_CONFIRMED** - appointment confirmed by staff
- **APPOINTMENT_NO_SHOW** - patient did not show up
- **APPOINTMENT_REMINDER_24H** - 24-hour reminder
- **APPOINTMENT_REMINDER_12H** - 12-hour reminder

---

## Reports

### Available Reports

1. **Appointment list** - appointment report for specific date range
2. **Patient visit history** - visit history for selected patient
3. **Room occupancy** - clinic occupancy statistics

### Generate Report

1. Navigate to **"Reports"** tab
2. Select report type
3. Set parameters:
   - Date range
   - Doctor (optional)
   - Location (optional)
4. Tap **"Generate"**

### Download Report

1. After generating report, tap **"Download"**
2. Report will be downloaded as PDF file
3. You can open or share it

---

## Account Settings

### Edit Profile

1. Navigate to **"Account"** tab
2. Tap **"Edit profile"**
3. Change required fields:
   - First and last name
   - Phone
   - Avatar
4. Tap **"Save"**

### Change Password

1. Navigate to **"Account"** tab
2. Tap **"Change password"**
3. Enter:
   - Current password
   - New password
   - Confirm new password
4. Tap **"Save"**

### Delete Account

1. Navigate to **"Account"** tab
2. Tap **"Delete account"**
3. Confirm deletion
4. Account will be deleted

---

## Frequently Asked Questions

### How to reset password?

1. On login screen tap **"Forgot password?"**
2. Enter email
3. Check email inbox
4. Tap password reset link
5. Enter new password

### How to change clinic?

1. Navigate to **"Account"** tab
2. Tap **"Change clinic"**
3. Select clinic from list
4. Confirm change

### How to add staff with user account?

1. Staff member must first create account in system
2. Then in **"Business"** -> **"Staff"** tab
3. Tap **"Add staff member"**
4. Select **"Link to existing account"** option
5. Enter staff member's email

### How to view dentist schedule?

1. Navigate to **"Schedule"** tab
2. Select dentist from list
3. Their work schedule will be displayed

---
