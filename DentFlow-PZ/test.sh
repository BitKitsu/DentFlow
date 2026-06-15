#!/bin/bash

# ==============================================================================
# DentFlow Backend Test Runner
# ==============================================================================

set -e

echo "============================================================"
echo "DentFlow -- wszystkie testy"
echo "============================================================"

# ---- Unit tests (Mockito, no Spring context) ----

echo ""
echo "1. PatientServiceTest [core-service/patient]"
echo "   - Wyszukiwanie, dodawanie, usuwanie pacjentow."
mvn test -Dtest=PatientServiceTest -Dsurefire.failIfNoSpecifiedTests=false -pl core-service/patient -am

echo ""
echo "2. StaffMemberServiceTest [core-service/clinic]"
echo "   - Zarzadzanie personelem, aktualizacja danych."
mvn test -Dtest=StaffMemberServiceTest -Dsurefire.failIfNoSpecifiedTests=false -pl core-service/clinic -am

echo ""
echo "3. AuthServiceTest [identity-service]"
echo "   - Rejestracja, logowanie, walidacja."
mvn test -Dtest=AuthServiceTest -Dsurefire.failIfNoSpecifiedTests=false -pl identity-service -am

echo ""
echo "4. NotificationServiceTest [core-service/notification]"
echo "   - Powiadomienia in-app."
mvn test -Dtest=NotificationServiceTest -Dsurefire.failIfNoSpecifiedTests=false -pl core-service/notification -am

echo ""
echo "5. SchedulingServiceTest [core-service/scheduling]"
echo "   - Sloty grafiku, blokady czasu, walidacja."
mvn test -Dtest=SchedulingServiceTest -Dsurefire.failIfNoSpecifiedTests=false -pl core-service/scheduling -am

echo ""
echo "6. AppointmentServiceTest [core-service/reservation]"
echo "   - Tworzenie, anulowanie, konflikt wizyt."
mvn test -Dtest=AppointmentServiceTest -Dsurefire.failIfNoSpecifiedTests=false -pl core-service/reservation -am

echo ""
echo "7. DentFlowPdfGeneratorTest [pdf-generator]"
echo "   - Generowanie raportow PDF."
mvn test -Dtest=DentFlowPdfGeneratorTest -Dsurefire.failIfNoSpecifiedTests=false -pl pdf-generator -am

# ---- Web layer tests (WebMvcTest) ----

echo ""
echo "8. AuthControllerTest [identity-service]"
echo "   - Kontroler auth: HTTP statusy, walidacja requestow."
mvn test -Dtest=AuthControllerTest -Dsurefire.failIfNoSpecifiedTests=false -pl identity-service -am

# ---- Validation tests (no Spring context) ----

echo ""
echo "9. RegisterRequestValidationTest [identity-service]"
echo "   - Walidacja DTO rejestracji (Jakarta Validation)."
mvn test -Dtest=RegisterRequestValidationTest -Dsurefire.failIfNoSpecifiedTests=false -pl identity-service -am

# ---- Integration tests (SpringBootTest + H2) ----

echo ""
echo "10. AuthFlowIntegrationTest [identity-service]"
echo "    - Full flow: register -> login -> change password."
mvn test -Dtest=AuthFlowIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false -pl identity-service -am

echo ""
echo "11. ReservationFlowIntegrationTest [core-service/reservation]"
echo "    - Full flow: create -> update -> cancel appointment."
mvn test -Dtest=ReservationFlowIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false -pl core-service/reservation -am

echo ""
echo "12. AppointmentRepositoryTest [core-service/reservation]"
echo "    - Repository: zapytania JPA, sortowanie, daty."
mvn test -Dtest=AppointmentRepositoryTest -Dsurefire.failIfNoSpecifiedTests=false -pl core-service/reservation -am

echo ""
echo "13. AppointmentNotificationIntegrationTest [core-service/reservation] (@Disabled)"
echo "    - Rezerwacja -> powiadomienie (wylaczone: konflikt @Transactional z @Async)."
mvn test -Dtest=AppointmentNotificationIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false -pl core-service/reservation -am || true

# ---- Spring context load tests (SpringBootTest + H2) ----

echo ""
echo "14. CoreServiceApplicationTests [core-service/core-app]"
echo "    - Spring context loads with H2 (Flyway disabled)."
mvn test -Dtest=CoreServiceApplicationTests -Dsurefire.failIfNoSpecifiedTests=false -pl core-service/core-app -am

echo ""
echo "============================================================"
echo "Wszystkie testy zakonczone."
echo "============================================================"
