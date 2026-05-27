package com.dentflow.core.clinic.api;

import com.dentflow.core.clinic.application.StaffMemberService;
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
@RequestMapping("/tenants/{tenantId}/staff")
@Tag(name = "Staff", description = "Zarządzanie personelem w gabinecie")
@SecurityRequirement(name = "bearerAuth")
public class StaffMemberController {

    private final StaffMemberService staffMemberService;

    public StaffMemberController(StaffMemberService staffMemberService) {
        this.staffMemberService = staffMemberService;
    }

    @GetMapping
    @Operation(summary = "Lista pracowników gabinetu")
    public ResponseEntity<List<StaffMemberResponse>> getStaffMembers(@PathVariable Long tenantId) {
        return ResponseEntity.ok(staffMemberService.getStaffMembers(tenantId));
    }

    @GetMapping("/{staffId}")
    @Operation(summary = "Pobranie szczegółow pracownika")
    public ResponseEntity<StaffMemberResponse> getStaffMember(
            @PathVariable Long tenantId,
            @PathVariable Long staffId) {
        return ResponseEntity.ok(staffMemberService.getStaffMember(tenantId, staffId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @Operation(summary = "Dodanie pracownika do gabinetu")
    public ResponseEntity<StaffMemberResponse> addStaffMember(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateStaffMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(staffMemberService.addStaffMember(tenantId, request));
    }

    @PutMapping("/{staffId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @Operation(summary = "Zaktualizowanie danych pracownika")
    public ResponseEntity<StaffMemberResponse> updateStaffMember(
            @PathVariable Long tenantId,
            @PathVariable Long staffId,
            @Valid @RequestBody UpdateStaffMemberRequest request) {
        return ResponseEntity.ok(staffMemberService.updateStaffMember(tenantId, staffId, request));
    }

    @DeleteMapping("/{staffId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @Operation(summary = "Usunięcie pracownika")
    public ResponseEntity<Void> deleteStaffMember(
            @PathVariable Long tenantId,
            @PathVariable Long staffId) {
        staffMemberService.deleteStaffMember(tenantId, staffId);
        return ResponseEntity.noContent().build();
    }
}
