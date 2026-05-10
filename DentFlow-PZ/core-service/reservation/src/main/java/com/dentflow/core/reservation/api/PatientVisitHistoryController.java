package com.dentflow.core.reservation.api;

import com.dentflow.core.reservation.application.PatientVisitHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dentflow.pdf.DentFlowPdfGenerator;
import com.dentflow.pdf.model.PatientVisitHistoryReportData;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/tenants/{tenantId}/patients/{patientId}/visits")
public class PatientVisitHistoryController {

    private final PatientVisitHistoryService historyService;

    public PatientVisitHistoryController(PatientVisitHistoryService historyService) {
        this.historyService = historyService;
    }

    /**
     * GET /tenants/{tenantId}/patients/{patientId}/visits
     * Zwraca pełną historię wizyt pacjenta w formacie PDF.
     *
     * Opcjonalny parametr ?status=COMPLETED filtruje po statusie.
     */
    @GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getPatientVisitHistory(
            @PathVariable Long tenantId,
            @PathVariable Long patientId,
            @RequestParam(required = false) String status) {

        List<PatientVisitHistoryDTO> result = (status != null && !status.isBlank())
                ? historyService.getPatientHistoryByStatus(tenantId, patientId, status)
                : historyService.getPatientHistory(tenantId, patientId);

        List<PatientVisitHistoryReportData.VisitRow> visits = result.stream()
                .map(dto -> new PatientVisitHistoryReportData.VisitRow(
                        dto.startAt().toString(),
                        "Lekarz ID: " + dto.dentistStaffId(),
                        "Usługa ID: " + dto.serviceItemId(),
                        dto.status(),
                        dto.notes()
                )).toList();

        PatientVisitHistoryReportData data = new PatientVisitHistoryReportData(
                "DentFlow Clinic",
                "Pacjent " + patientId, "",
                "", "",
                (status != null && !status.isBlank()) ? "Status: " + status : "Wszystkie",
                visits
        );

        try {
            byte[] pdf = new DentFlowPdfGenerator().generatePatientHistory(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"historia_pacjenta_" + patientId + ".pdf\"")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
