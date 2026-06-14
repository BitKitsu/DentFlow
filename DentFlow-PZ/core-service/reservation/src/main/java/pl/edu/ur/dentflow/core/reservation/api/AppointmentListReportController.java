package pl.edu.ur.dentflow.core.reservation.api;

import pl.edu.ur.dentflow.core.reservation.application.AppointmentListReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST endpoint for Report 1: Appointment List.
 * SCRUM-60
 *
 * GET /tenants/{tenantId}/reports/appointments
 * Parameters:
 *   ?from=     - start date (YYYY-MM-DD), required
 *   ?to=       - end date (YYYY-MM-DD), required
 *   ?status=   - status filter (SCHEDULED / COMPLETED / CANCELLED), optional
 *   ?dentistId=– dentist ID filter, optional
 *
 * Returns PDF file (application/pdf).
 */
@RestController
@RequestMapping("/tenants/{tenantId}/reports/appointments")
public class AppointmentListReportController {

    private final AppointmentListReportService reportService;

    public AppointmentListReportController(AppointmentListReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'DENTIST', 'RECEPTIONIST', 'ASSISTANT', 'PATIENT')")
    public ResponseEntity<byte[]> getAppointmentListReport(
            @PathVariable Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long dentistId) {

        byte[] pdf = reportService.generateReport(tenantId, from, to, status, dentistId);

        String filename = "lista_wizyt_" + from + "_" + to + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
