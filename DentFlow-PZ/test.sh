#!/bin/bash

# ==============================================================================
# DentFlow Backend Test Runner
# ==============================================================================

echo "------------------------------------------------------------"
echo "Uruchamianie testów jednostkowych DentFlow (Core Service)..."
echo "------------------------------------------------------------"

echo "1. Uruchamianie testów modułu Pacjentów (PatientServiceTest)..."
echo "   - Wyszukiwanie, dodawanie, usuwanie pacjentów."
echo "   - Testy niepoprawności (404 przy braku pacjenta)."
mvn test -Dtest=PatientServiceTest -Dsurefire.failIfNoSpecifiedTests=false -pl core-service/patient

echo ""
echo "2. Uruchamianie testów modułu Kliniki (StaffMemberServiceTest)..."
echo "   - Zarządzanie personelem, aktualizacja danych."
echo "   - Testy niepoprawności (404 przy braku personelu/gabinetu)."
mvn test -Dtest=StaffMemberServiceTest -Dsurefire.failIfNoSpecifiedTests=false -pl core-service/clinic

echo ""
echo "3. Uruchamianie testów modułu Tożsamości (AuthServiceTest) [SCRUM-51]..."
echo "   - Rejestracja, walidacja email."
echo "   - Logowanie, weryfikacja statusu."
mvn test -Dtest=AuthServiceTest -Dsurefire.failIfNoSpecifiedTests=false -pl identity-service

echo ""
echo "4. Uruchamianie testów modułu Powiadomień (NotificationServiceTest) [SCRUM-55]..."
echo "   - Powiadomienia in-app: pobieranie, oznaczanie."
mvn test -Dtest=NotificationServiceTest -Dsurefire.failIfNoSpecifiedTests=false -pl core-service/notification

echo ""
echo "3. Uruchamianie testów modułu Grafiku pracy (SchedulingServiceTest)..."
echo "   - Sloty grafiku: dodawanie, pobieranie, usuwanie."
echo "   - Blokady czasu: dodawanie, pobieranie, usuwanie."
echo "   - Walidacja: endAt musi być po startAt."
mvn test -Dtest=SchedulingServiceTest -Dsurefire.failIfNoSpecifiedTests=false -pl core-service/scheduling

echo ""
echo "4. Uruchamianie testów modułu Rezerwacji wizyt (AppointmentServiceTest)..."
echo "   - Tworzenie wizyty: sukces i walidacja konfliktu (409)."
echo "   - Anulowanie i zakończenie wizyty."
echo "   - Walidacja: endAt musi być po startAt."
echo "   - Testy niepoprawności (404 przy braku wizyty)."
mvn test -Dtest=AppointmentServiceTest -Dsurefire.failIfNoSpecifiedTests=false -pl core-service/reservation

echo ""
echo "------------------------------------------------------------"
echo "Testy zakończone."
echo "------------------------------------------------------------"
