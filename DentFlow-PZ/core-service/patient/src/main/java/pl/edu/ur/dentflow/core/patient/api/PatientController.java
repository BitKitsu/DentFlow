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

/**
 * REST controller managing patients in the DentFlow system.
 *
 * <p>Patient records are linked to identity users via the userId field.
 * The /ensure endpoint automatically creates a patient record when a user
 * first accesses patient-related functionality.</p>
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /tenants/{tenantId}/patients - list/search patients (OWNER, DENTIST, ASSISTANT)</li>
 *   <li>GET /tenants/{tenantId}/patients/{id} - get patient details</li>
 *   <li>POST /tenants/{tenantId}/patients - add patient (OWNER, DENTIST)</li>
 *   <li>PUT /tenants/{tenantId}/patients/{id} - update patient (OWNER, DENTIST)</li>
 *   <li>DELETE /tenants/{tenantId}/patients/{id} - delete patient (OWNER only)</li>
 *   <li>POST /tenants/{tenantId}/patients/ensure - find or create patient for user</li>
 * </ul>
 *
 * @see pl.edu.ur.dentflow.core.patient.application.PatientService
 */
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
    @PreAuthorize("hasAnyRole('OWNER', 'DENTIST', 'RECEPTIONIST', 'ASSISTANT')")
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
