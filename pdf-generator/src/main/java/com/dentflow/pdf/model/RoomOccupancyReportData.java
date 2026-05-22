package com.dentflow.pdf.model;

import java.util.List;

/**
 * Parametry i dane dla Raportu 2: Obłożenie gabinetu.
 *
 * Zawartość PDF:
 * - Nagłówek: nazwa gabinetu, miesiąc/rok, opcjonalnie lokalizacja
 * - Wykres słupkowy (ASCII/tabela): liczba wizyt dziennie
 * - Tabela: Lekarz | Liczba wizyt | Liczba godzin pracy | % wykorzystania slotów
 * - Statystyki: średni czas wizyty, najpopularniejsze usługi, wskaźnik no-show
 */
public record RoomOccupancyReportData(
        String clinicName,
        int month,
        int year,
        String locationFilter,      // null = wszystkie lokalizacje
        List<DailyStats> dailyStats,
        List<DoctorStats> doctorStats,
        double avgAppointmentMinutes,
        List<String> topServices,   // top 3 najpopularniejsze usługi
        double noShowRate           // procent no-show (0-100)
) {
    public record DailyStats(
            int dayOfMonth,
            long appointmentCount
    ) {}

    public record DoctorStats(
            String doctorFullName,
            long appointmentCount,
            double workHours,
            double slotUtilizationPercent
    ) {}
}
