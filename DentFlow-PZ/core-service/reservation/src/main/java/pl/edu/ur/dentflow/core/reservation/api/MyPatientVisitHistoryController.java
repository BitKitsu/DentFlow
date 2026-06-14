package pl.edu.ur.dentflow.core.reservation.api;

import pl.edu.ur.dentflow.core.clinic.domain.Tenant;
import pl.edu.ur.dentflow.core.clinic.infrastructure.TenantRepository;
import pl.edu.ur.dentflow.core.patient.infrastructure.PatientRepository;
import pl.edu.ur.dentflow.core.reservation.application.PatientVisitHistoryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/tenants/{tenantId}/patients/my/visits")
public class MyPatientVisitHistoryController {

    private final PatientVisitHistoryService historyService;
    private final TenantRepository tenantRepository;
    private final PatientRepository patientRepository;

    public MyPatientVisitHistoryController(PatientVisitHistoryService historyService,
                                            TenantRepository tenantRepository,
                                            PatientRepository patientRepository) {
        this.historyService = historyService;
        this.tenantRepository = tenantRepository;
        this.patientRepository = patientRepository;
    }

    @GetMapping("/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getMyVisitHistoryPdf(
            @PathVariable Long tenantId,
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        Long userId = authentication.getCredentials() instanceof Long
                ? (Long) authentication.getCredentials()
                : 0L;

        Long patientId = patientRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Nie znaleziono profilu pacjenta dla bieżącego użytkownika"))
                .getId();

        String clinicName = tenantRepository.findById(tenantId)
                .map(Tenant::getName)
                .orElse("Gabinet");

        try {
            byte[] pdf = historyService.generatePdf(tenantId, patientId, clinicName, status, from, to);
            String filename = "historia_wizyt.pdf";
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
