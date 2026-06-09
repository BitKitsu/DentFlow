package pl.edu.ur.dentflow.pdf.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Parameters and data for Report 1: Appointment List.
 *
 * PDF contents:
 * - Header: clinic name, date range
 * - Table: Date/time | Patient | Doctor | Service | Status
 * - Summary: total appointments, cancelled, no-show
 */
public record AppointmentListReportData(
        String clinicName,
        LocalDate dateFrom,
        LocalDate dateTo,
        String doctorFilter,       // null = all doctors
        String locationFilter,     // null = all locations
        String statusFilter,       // null = all statuses
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
