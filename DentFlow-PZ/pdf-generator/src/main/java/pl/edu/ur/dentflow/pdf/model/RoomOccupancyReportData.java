package pl.edu.ur.dentflow.pdf.model;

import java.util.List;

/**
 * Parameters and data for Report 2: Room Occupancy.
 *
 * PDF contents:
 * - Header: clinic name, month/year, optional location
 * - Bar chart (ASCII/table): daily appointment count
 * - Table: Doctor | Appointment count | Working hours | Slot utilization %
 * - Statistics: average appointment duration, most popular services, no-show rate
 */
public record RoomOccupancyReportData(
        String clinicName,
        int month,
        int year,
        String locationFilter,      // null = all locations
        List<DailyStats> dailyStats,
        List<DoctorStats> doctorStats,
        double avgAppointmentMinutes,
        List<String> topServices,   // top 3 most popular services
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
