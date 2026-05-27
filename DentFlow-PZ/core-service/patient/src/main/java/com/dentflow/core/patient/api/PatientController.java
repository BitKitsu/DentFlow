package com.dentflow.core.patient.api;

import com.dentflow.core.patient.application.PatientService;
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
@Tag(name = "Patients", description = "Zarządzanie pacjentami")
@SecurityRequirement(name = "bearerAuth")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DOCTOR')")
    @Operation(summary = "Lista pacjentów lub wyszukiwanie po imieniu/nazwisku/telefonie")
    public ResponseEntity<List<PatientResponse>> getPatients(
            @PathVariable Long tenantId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(patientService.getPatients(tenantId, search));
    }

    @GetMapping("/{patientId}")
    @Operation(summary = "Pobranie pacjenta")
    public ResponseEntity<PatientResponse> getPatient(
            @PathVariable Long tenantId,
            @PathVariable Long patientId) {
        return ResponseEntity.ok(patientService.getPatient(tenantId, patientId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DOCTOR')")
    @Operation(summary = "Dodanie pacjenta")
    public ResponseEntity<PatientResponse> addPatient(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreatePatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(patientService.addPatient(tenantId, request));
    }

    @PutMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DOCTOR')")
    @Operation(summary = "Aktualizacja pacjenta")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long tenantId,
            @PathVariable Long patientId,
            @Valid @RequestBody UpdatePatientRequest request) {
        return ResponseEntity.ok(patientService.updatePatient(tenantId, patientId, request));
    }

    @DeleteMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @Operation(summary = "Usunięcie pacjenta")
    public ResponseEntity<Void> deletePatient(
            @PathVariable Long tenantId,
            @PathVariable Long patientId) {
        patientService.deletePatient(tenantId, patientId);
        return ResponseEntity.noContent().build();
    }
}
