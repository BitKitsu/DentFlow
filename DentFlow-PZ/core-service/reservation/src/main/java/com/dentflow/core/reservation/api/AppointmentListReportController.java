package com.dentflow.core.reservation.api;

import com.dentflow.core.reservation.application.AppointmentListReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST endpoint dla Raportu 1: Lista wizyt.
 * SCRUM-60
 *
 * GET /tenants/{tenantId}/reports/appointments
 * Parametry:
 *   ?from=     – data początkowa (YYYY-MM-DD), wymagana
 *   ?to=       – data końcowa   (YYYY-MM-DD), wymagana
 *   ?status=   – filtr statusu (SCHEDULED / COMPLETED / CANCELLED), opcjonalny
 *   ?dentistId=– filtr ID lekarza, opcjonalny
 *
 * Zwraca plik PDF (application/pdf).
 */
@RestController
@RequestMapping("/tenants/{tenantId}/reports/appointments")
public class AppointmentListReportController {

    private final AppointmentListReportService reportService;

    public AppointmentListReportController(AppointmentListReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)
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
                .body(pdf);
    }
}
