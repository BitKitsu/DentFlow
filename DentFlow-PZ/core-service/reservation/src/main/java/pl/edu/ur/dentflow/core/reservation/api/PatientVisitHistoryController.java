package pl.edu.ur.dentflow.core.reservation.api;

import pl.edu.ur.dentflow.core.clinic.domain.Tenant;
import pl.edu.ur.dentflow.core.clinic.infrastructure.TenantRepository;
import pl.edu.ur.dentflow.core.reservation.application.PatientVisitHistoryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller providing patient visit history.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /tenants/{tenantId}/patients/{patientId}/visits - JSON visit history</li>
 *   <li>GET /tenants/{tenantId}/patients/{patientId}/visits/pdf - PDF visit history</li>
 * </ul>
 *
 * <p>Optional status parameter filters by SCHEDULED, COMPLETED, or CANCELLED.</p>
 *
 * @see pl.edu.ur.dentflow.core.reservation.application.PatientVisitHistoryService
 */
@RestController
@RequestMapping("/tenants/{tenantId}/patients/{patientId}/visits")
public class PatientVisitHistoryController {

    private final PatientVisitHistoryService historyService;
    private final TenantRepository tenantRepository;

    public PatientVisitHistoryController(PatientVisitHistoryService historyService,
                                          TenantRepository tenantRepository) {
        this.historyService = historyService;
        this.tenantRepository = tenantRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PatientVisitHistoryDTO>> getPatientVisitHistoryJson(
            @PathVariable Long tenantId,
            @PathVariable Long patientId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<PatientVisitHistoryDTO> result = (status != null && !status.isBlank())
                ? historyService.getPatientHistoryByStatus(tenantId, patientId, status, from, to)
                : historyService.getPatientHistory(tenantId, patientId, from, to);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getPatientVisitHistoryPdf(
            @PathVariable Long tenantId,
            @PathVariable Long patientId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        String clinicName = tenantRepository.findById(tenantId)
                .map(Tenant::getName)
                .orElse("Gabinet");

        try {
            byte[] pdf = historyService.generatePdf(tenantId, patientId, clinicName, status, from, to);
            String filename = "historia_pacjenta_" + patientId + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "PDF generation error: " + e.getMessage());
        }
    }
}
