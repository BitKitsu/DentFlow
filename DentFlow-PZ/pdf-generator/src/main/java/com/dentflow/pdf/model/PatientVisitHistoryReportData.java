package com.dentflow.pdf.model;

import java.util.List;

/**
 * Parametry i dane dla Raportu 3: Historia wizyt pacjenta.
 *
 * Zawartość PDF:
 * - Dane pacjenta: imię, nazwisko, telefon, email
 * - Tabela wizyt: Data | Lekarz | Usługa | Status | Notatki
 * - Podsumowanie: łączna liczba wizyt, data ostatniej wizyty
 */
public record PatientVisitHistoryReportData(
        String clinicName,
        String patientFirstName,
        String patientLastName,
        String patientPhone,
        String patientEmail,
        String dateRangeDescription,  // np. "Wszystkie" lub "01.01.2026 – 09.05.2026"
        List<VisitRow> visits
) {
    public record VisitRow(
            String date,
            String doctorFullName,
            String serviceName,
            String status,
            String notes
    ) {}
}
