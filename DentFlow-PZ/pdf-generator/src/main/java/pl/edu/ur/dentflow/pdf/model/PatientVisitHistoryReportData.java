package pl.edu.ur.dentflow.pdf.model;

import java.util.List;

/**
 * Parameters and data for Report 3: Patient Visit History.
 *
 * PDF contents:
 * - Patient data: first name, last name, phone, email
 * - Visit table: Date | Doctor | Service | Status | Notes
 * - Summary: total visits, last visit date
 */
public record PatientVisitHistoryReportData(
        String clinicName,
        String patientFirstName,
        String patientLastName,
        String patientPhone,
        String patientEmail,
        String dateRangeDescription,  // e.g. "All" or "01.01.2026 – 09.05.2026"
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
