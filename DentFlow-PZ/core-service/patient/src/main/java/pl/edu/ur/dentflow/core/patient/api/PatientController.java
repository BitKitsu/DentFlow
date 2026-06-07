package pl.edu.ur.dentflow.core.patient.api;

import pl.edu.ur.dentflow.core.patient.application.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenants/{tenantId}/patients")
@Tag(name = "Patients", description = "Patient management")
@SecurityRequirement(name = "bearerAuth")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'DENTIST', 'ASSISTANT')")
    @Operation(summary = "List patients or search by name/phone")
    public ResponseEntity<List<PatientResponse>> getPatients(
            @PathVariable Long tenantId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(patientService.getPatients(tenantId, search));
    }

    @GetMapping("/{patientId}")
    @Operation(summary = "Get patient")
    public ResponseEntity<PatientResponse> getPatient(
            @PathVariable Long tenantId,
            @PathVariable Long patientId) {
        return ResponseEntity.ok(patientService.getPatient(tenantId, patientId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'DENTIST')")
    @Operation(summary = "Add patient")
    public ResponseEntity<PatientResponse> addPatient(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreatePatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(patientService.addPatient(tenantId, request));
    }

    @PutMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('OWNER', 'DENTIST')")
    @Operation(summary = "Update patient")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long tenantId,
            @PathVariable Long patientId,
            @Valid @RequestBody UpdatePatientRequest request) {
        return ResponseEntity.ok(patientService.updatePatient(tenantId, patientId, request));
    }

    @DeleteMapping("/{patientId}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Delete patient")
    public ResponseEntity<Void> deletePatient(
            @PathVariable Long tenantId,
            @PathVariable Long patientId) {
        patientService.deletePatient(tenantId, patientId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ensure")
    @Operation(summary = "Find or create patient record for a user")
    public ResponseEntity<PatientResponse> ensurePatient(
            @PathVariable Long tenantId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "") String firstName,
            @RequestParam(defaultValue = "") String lastName,
            @RequestParam(defaultValue = "") String email) {
        return ResponseEntity.ok(patientService.ensurePatientForUser(tenantId, userId, firstName, lastName, email));
    }
}
