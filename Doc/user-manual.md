# DentFlow User Manual

## Table of Contents

1. [Introduction](#introduction)
2. [Registration and Login](#registration-and-login)
3. [Navigation](#navigation)
4. [Clinic Management](#clinic-management)
5. [Patient Management](#patient-management)
6. [Appointment Booking](#appointment-booking)
7. [Work Schedule](#work-schedule)
8. [Service Catalog](#service-catalog)
9. [Notifications](#notifications)
10. [Reports](#reports)
11. [Account Settings](#account-settings)
12. [FAQ](#faq)

---

## Introduction

DentFlow is a modern system for dental clinic management.
The application enables:

- Clinic and location management
- Patient records
- Appointment booking
- Work schedule management (breaks and blockers)
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
| OWNER | Clinic owner — full access to all features |
| DENTIST | Dentist — manage appointments and patients, view rooms/catalog/schedule |
| RECEPTIONIST | Receptionist — manage appointments and patients, manage rooms, view catalog/schedule |
| ASSISTANT | Dental assistant — read-only access to appointments, patients, rooms, catalog, schedule |
| PATIENT | Patient — view own appointments only |

---

## Registration and Login

### Register New Account

1. Open DentFlow application
2. Tap **"Zarejestruj się"** on the login screen
3. Fill the registration form (all fields are required):
   - Imię (first name)
   - Nazwisko (last name)
   - Numer telefonu (phone number)
   - E-mail
   - Ulica i numer (street and number)
   - Miasto (city)
   - Kod pocztowy (postal code)
   - Kraj (country)
   - Hasło (min. 8 characters)
   - Powtórz hasło (confirm password)
4. Tap **"ZAREJESTRUJ SIĘ"**
5. After successful registration, you will be redirected to the login screen

### Login

1. Open DentFlow application
2. Enter email and password
3. Tap **"ZALOGUJ SIĘ"**
4. After successful login, you will be redirected to the main screen

### Logout

1. Navigate to the **"Konto"** tab
2. Tap **"WYLOGUJ SIĘ"**
3. You will be logged out immediately

---

## Navigation

The application uses a bottom navigation bar with the following tabs:

| Tab | Label | Description |
|-----|-------|-------------|
| 1 | **Oferty** | Browse available services and book appointments |
| 2 | **Klinika** | Clinic management dashboard (staff users only) |
| 3 | **Wizyty** | View and manage your appointments |
| 4 | **Powiadomienia** | View notifications |
| 5 | **Konto** | Account settings and clinic creation |

The **"Klinika"** tab is only visible to staff users (owner, dentist, receptionist, assistant) who have a clinic assigned.

---

## Clinic Management

### Create Clinic

1. Navigate to the **"Konto"** tab
2. Tap **"UTWÓRZ KLINIKĘ"**
3. Fill the registration form:
   - Nazwa kliniki (clinic name)
   - Nazwa placówki (location/place name)
   - Ulica i numer (street and number)
   - Miasto (city)
   - Kod pocztowy (postal code)
   - Kraj (country)
4. Tap **"UTWÓRZ I ZAREJESTRUJ"**

### Clinic Dashboard

1. Navigate to the **"Klinika"** tab
2. The dashboard shows:
   - Clinic statistics cards (patients, appointments, services)
   - Menu items for clinic management

### Edit Clinic Data

1. Navigate to the **"Klinika"** tab
2. Tap **"Edytuj dane kliniki"**
3. Change required fields
4. Tap **"Zapisz dane"**

### Manage Staff

1. Navigate to the **"Klinika"** tab
2. Tap **"Pracownicy"**
3. The staff list is displayed
4. To add staff: tap the **person-add** icon (FAB, bottom-right)
5. To edit: tap the **⋮** (three-dot) menu on a staff member, then tap **"Edytuj"**
6. To delete: tap the **⋮** menu, then tap **"Usuń z gabinetu"**
7. To view profile: tap on the staff member card

> **Note:** Staff management is only available to clinic owners.

### Add Staff Member

1. Tap the **person-add** icon on the staff list screen
2. The form is divided into numbered sections:
   - **1. Sprawdź email pracownika** — enter email and tap **"Sprawdź"** to check if the user already exists
   - **2. Dane osobowe** — Imię, Nazwisko, Telefon
   - **3. Rola i dane zawodowe** — select Rola from dropdown (Dentysta, Recepcjonista, Asystent)
   - **4. Konto logowania** — Hasło tymczasowe (only shown for new users)
   - Bio / O mnie (optional)
3. Tap **"DODAJ"**

> **Note:** If the email check finds an existing user, their data will be auto-filled automatically.

### Edit Staff Working Hours

1. Tap on a staff member card to open their profile
2. Tap the **edit** icon next to "Godziny pracy"
3. Set working hours for each day of the week using checkboxes and time pickers
4. Tap **"Zapisz"**

---

## Patient Management

### View Patient List

1. Navigate to the **"Klinika"** tab
2. Tap **"Pacjenci"** (stat card or menu row)
3. The patient list is displayed with name, phone, and PESEL
4. Empty list shows **"Baza pacjentów jest pusta"**

### Add Patient

1. Tap the **"+"** FAB (bottom-right) on the patient list screen
2. The form is divided into numbered sections:
   - **1. Sprawdź email pacjenta** — enter email and tap **"Sprawdź"** to check if the user already exists
   - **2. Dane osobowe** — Imię, Nazwisko, Telefon, E-mail (opcjonalny)
   - **3. Dane medyczne** — Numer PESEL (11 cyfr), Płeć (dropdown: Kobieta, Mężczyzna, Inna, Nie podano), Data urodzenia (opcjonalna)
   - **4. Adres (opcjonalny)** — Ulica i numer, Kod pocztowy, Miasto
3. Tap **"Zapisz"**

### Edit Patient

1. Tap the **⋮** (three-dot) menu on a patient
2. Tap **"Edytuj"**
3. Change required fields
4. Tap **"Zapisz"**

### View Patient History

1. Tap the **⋮** menu on a patient
2. Tap **"Historia wizyt"**
3. The visit history dialog is displayed

### Delete Patient

1. Tap the **⋮** menu on a patient
2. Tap **"Usuń"**
3. The patient is deleted immediately

### View Patient Profile

1. Tap on a patient card
2. The patient detail dialog shows avatar, phone, email, date of birth, PESEL, and address

---

## Appointment Booking

### Create New Appointment

1. Navigate to the **"Klinika"** tab
2. Tap **"Wizyty w klinice"** or use the appointment creation flow
3. Follow the 3-step wizard:
   - **Step 1: Lekarz** — select a dentist
   - **Step 2: Termin** — select date and time slot
   - **Step 3: Potwierdzenie** — review and confirm booking

### View Appointments

1. Navigate to the **"Wizyty"** tab
2. The default view is **Calendar** mode — a monthly calendar grid
3. Tap a day to see appointments for that date
4. Tap the **history** icon to switch to **"Historia Wizyt"** (Visit History) mode — a sorted list of past visits

### Cancel Appointment

1. Select an appointment
2. Tap **"Anuluj wizytę"**
3. The status will change to **CANCELLED**

> **Note:** Cancellation can only be performed by non-patient, non-read-only users on their own appointments.

### Complete Appointment

1. Select an appointment
2. Tap **"Zakończ wizytę"**
3. The status will change to **COMPLETED**

### Appointment Statuses

- **SCHEDULED** — appointment is booked
- **CONFIRMED** — appointment is confirmed by staff
- **COMPLETED** — appointment has been completed
- **CANCELLED** — appointment was cancelled
- **NO_SHOW** — patient did not show up

### Clinic Appointments View

1. Navigate to the **"Klinika"** tab
2. Tap **"Wizyty w klinice"**
3. View all appointments in the clinic with calendar and action buttons
4. Actions available: **"Potwierdź"** (Confirm), **"Zakończ"** (Complete), **"Anuluj"** (Cancel)

> **Note:** Clinic appointments view is only available to staff users.

---

## Work Schedule

### View Breaks/Blockers

1. Navigate to the **"Klinika"** tab
2. Tap **"Grafik pracy"**
3. The screen title is **"Przerwy"** (Breaks)
4. A monthly calendar is displayed with left/right arrows for navigation
5. Click a day to see blockers/breaks for that date
6. Empty days show **"Brak przerw / urlopów na ten dzień"**

### Add Break/Blocker

1. Tap the **"+"** FAB (bottom-right)
2. Fill the form:
   - Data (date) — tap to open date picker
   - Od (start time) — tap to open time picker
   - Do (end time) — tap to open time picker
   - Powód (reason) — free text (e.g., urlop, przerwa)
   - Lekarz (staff) — dropdown with staff list, or **"Wszyscy"** (All)
3. Tap **"Zapisz"**

> **Note:** Break management is only available to clinic owners.

### Delete Break/Blocker

1. Tap on a blocker item
2. Tap **"Usuń"**
3. Confirm deletion in the dialog

### Working Hours

Working hours for individual staff members are managed separately via the staff profile:

1. Navigate to **"Pracownicy"**
2. Tap on a staff member card
3. Tap the **edit** icon next to "Godziny pracy"
4. Set hours for each day of the week
5. Tap **"Zapisz"**

---

## Service Catalog

### View Price List

1. Navigate to the **"Klinika"** tab
2. Tap **"Cennik usług"** (or the "Zabiegi" stat card)
3. The service list is displayed with name, duration, and price
4. Inactive services are shown with gray styling
5. Empty list shows **"Brak usług w cenniku"**

### Add Service

> **Note:** Only available to clinic owners.

1. Tap the **"+"** icon in the top app bar
2. Fill the form:
   - Nazwa zabiegu (service/treatment name, min. 2 characters)
   - Cena (zł) — price in PLN
   - Czas trwania (min) — duration in minutes
   - Usługa aktywna — toggle switch
3. Tap **"Zapisz"**

### Edit Service

> **Note:** Only available to clinic owners.

1. Tap the **pencil** icon on a service
2. Change required fields
3. Tap **"Zapisz"**

### Delete Service

> **Note:** Only available to clinic owners.

1. Tap the **trash** icon on a service
2. The service is deleted immediately

---

## Notifications

### View Notifications

1. Navigate to the **"Powiadomienia"** tab (bell icon in bottom navigation)
2. The notification list is displayed
3. Unread notifications have a blue dot indicator and different card styling

### Filter Notifications

1. Use the filter chips at the top:
   - **"Wszystkie (N)"** — show all notifications
   - **"Nieprzeczytane (N)"** — show only unread notifications

### Refresh Notifications

1. Tap the **refresh** icon in the top app bar

### Mark as Read

1. Tap on a notification
2. It will be marked as read and the unread count will decrease

### Mark All as Read

1. Tap **"Oznacz wszystkie"** (appears when there are unread notifications)
2. All notifications will be marked as read

### Notification Types

- **APPOINTMENT** — new appointment created
- **APPOINTMENT_CANCELLED** — appointment cancelled
- **APPOINTMENT_COMPLETED** — appointment completed
- **APPOINTMENT_CONFIRMED** — appointment confirmed by staff
- **APPOINTMENT_NO_SHOW** — patient did not show up
- **APPOINTMENT_REMINDER_24H** — 24-hour reminder
- **APPOINTMENT_REMINDER_12H** — 12-hour reminder

---

## Reports

> **Note:** Reports are only available to clinic owners.

### Access Reports

Reports can be accessed from:
- The **"Klinika"** tab → **"Raporty PDF"** menu item
- The **"Konto"** tab → **"RAPORTY PDF"** button

### Available Reports

1. **Lista wizyt** (Appointment list) — appointment report for a specific date range
2. **Obłożenie gabinetów** (Room occupancy) — room occupancy statistics with room selector
3. **Historia pacjenta** (Patient visit history) — visit history for a selected patient

### Download Report

1. Select report type
2. Set parameters:
   - Lista wizyt: date range (from/to)
   - Obłożenie gabinetów: date range + room selector
   - Historia pacjenta: patient selector
3. Tap **"Pobierz PDF"**
4. Report will be downloaded as a PDF file
5. You can open or share it

---

## Account Settings

### Access Account Settings

1. Navigate to the **"Konto"** tab

### Edit Account Data

1. Tap **"Dane konta"**
2. Change required fields:
   - Imię (first name)
   - Nazwisko (last name)
   - Numer telefonu (phone)
   - Adres e-mail
   - Ulica i numer (street and number)
   - Miasto (city)
   - Kod pocztowy (postal code)
   - Kraj (country)
   - Avatar (tap to upload via camera or gallery)
3. Tap **"Zapisz"**

### Change Password

1. Tap **"Dane konta"**
2. Scroll to the **"Zmiana hasła"** section
3. Enter:
   - Current password
   - New password
   - Confirm new password
4. Tap **"Zmień hasło"**

### Delete Account

1. Tap **"Usuń konto"**
2. Confirm deletion in the dialog:
   - "Czy na pewno chcesz bezpowrotnie usunąć swoje konto? Tej operacji nie można cofnąć."
3. Tap **"Usuń"**

### Application Settings

1. Tap **"Ustawienia aplikacji"**
2. Toggle dark/light theme

---

## FAQ

### How to add staff with a user account?

1. Staff member must first create an account in the system (via registration)
2. Navigate to **"Klinika"** → **"Pracownicy"**
3. Tap the **person-add** icon
4. Enter the staff member's email in section 1
5. Tap **"Sprawdź"** — if the user exists, their data will be auto-filled automatically
6. Complete the remaining fields and tap **"DODAJ"**

### How to view dentist schedule?

1. Navigate to the **"Klinika"** tab
2. Tap **"Grafik pracy"**
3. Use the calendar to navigate to the desired date
4. Tap a day to see breaks/blockers for that date

### How to generate a report?

1. Navigate to the **"Klinika"** tab → **"Raporty PDF"** (or **"Konto"** → **"RAPORTY PDF"**)
2. Select report type
3. Set parameters
4. Tap **"Pobierz PDF"**

---

## Contact

If you have any problems, contact us:
- Email: support@dentflow.pl
- Phone: +48 123 456 789
- Website: https://dentflow.pl
