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
mvn test -Dtest=PatientServiceTest -Dsurefire.failIfNoSpecifiedTests=false

echo ""
echo "2. Uruchamianie testów modułu Kliniki (StaffMemberServiceTest)..."
echo "   - Zarządzanie personelem, aktualizacja danych."
echo "   - Testy niepoprawności (404 przy braku personelu/gabinetu)."
mvn test -Dtest=StaffMemberServiceTest -Dsurefire.failIfNoSpecifiedTests=false

echo "------------------------------------------------------------"
echo "Testy zakończone."
echo "------------------------------------------------------------"
