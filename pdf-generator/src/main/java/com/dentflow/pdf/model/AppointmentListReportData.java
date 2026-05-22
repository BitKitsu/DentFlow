package com.dentflow.pdf.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Parametry i dane dla Raportu 1: Lista wizyt.
 *
 * Zawartość PDF:
 * - Nagłówek: nazwa gabinetu, zakres dat
 * - Tabela: Data/godzina | Pacjent | Lekarz | Usługa | Status
 * - Podsumowanie: łączna liczba wizyt, anulowanych, no-show
 */
public record AppointmentListReportData(
        String clinicName,
        LocalDate dateFrom,
        LocalDate dateTo,
        String doctorFilter,       // null = wszyscy lekarze
        String locationFilter,     // null = wszystkie lokalizacje
        String statusFilter,       // null = wszystkie statusy
        List<AppointmentRow> appointments
) {
    public record AppointmentRow(
            String dateTime,
            String patientFullName,
            String doctorFullName,
            String serviceName,
            String status
    ) {}
}
