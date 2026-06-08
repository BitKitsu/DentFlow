package pl.edu.ur.dentflow.core.clinic.api;

import pl.edu.ur.dentflow.core.clinic.application.StaffMemberService;
import pl.edu.ur.dentflow.core.clinic.application.StaffWorkingHoursService;
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
 * REST controller managing staff members in the DentFlow system.
 *
 * <p>Staff members represent employees (dentists, assistants, receptionists)
 * within a clinic. Each staff member is linked to an identity user via userId
 * and has a role that determines their permissions.</p>
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /tenants/{tenantId}/staff - list staff members</li>
 *   <li>GET /tenants/{tenantId}/staff/{staffId} - staff member details</li>
 *   <li>POST /tenants/{tenantId}/staff - add staff member (OWNER only)</li>
 *   <li>PUT /tenants/{tenantId}/staff/{staffId} - update staff member (OWNER only)</li>
 *   <li>DELETE /tenants/{tenantId}/staff/{staffId} - delete staff member (OWNER only)</li>
 *   <li>POST /tenants/{tenantId}/staff/sync-from-user - sync profile data</li>
 *   <li>GET /tenants/{tenantId}/staff/{staffId}/working-hours - get working hours</li>
 *   <li>PUT /tenants/{tenantId}/staff/{staffId}/working-hours - update working hours</li>
 * </ul>
 *
 * @see pl.edu.ur.dentflow.core.clinic.application.StaffMemberService
 * @see pl.edu.ur.dentflow.core.clinic.application.StaffWorkingHoursService
 */
@RestController
@RequestMapping("/tenants/{tenantId}/staff")
@Tag(name = "Staff", description = "Staff management in clinic")
@SecurityRequirement(name = "bearerAuth")
public class StaffMemberController {

    private final StaffMemberService staffMemberService;
    private final StaffWorkingHoursService workingHoursService;

    public StaffMemberController(StaffMemberService staffMemberService,
                                  StaffWorkingHoursService workingHoursService) {
        this.staffMemberService = staffMemberService;
        this.workingHoursService = workingHoursService;
    }

    @GetMapping
    @Operation(summary = "List clinic staff members")
    public ResponseEntity<List<StaffMemberResponse>> getStaffMembers(@PathVariable Long tenantId) {
        return ResponseEntity.ok(staffMemberService.getStaffMembers(tenantId));
    }

    @GetMapping("/{staffId}")
    @Operation(summary = "Get staff member details")
    public ResponseEntity<StaffMemberResponse> getStaffMember(
            @PathVariable Long tenantId,
            @PathVariable Long staffId) {
        return ResponseEntity.ok(staffMemberService.getStaffMember(tenantId, staffId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER')")
    @Operation(summary = "Add staff member to clinic")
    public ResponseEntity<StaffMemberResponse> addStaffMember(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateStaffMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(staffMemberService.addStaffMember(tenantId, request));
    }

    @PutMapping("/{staffId}")
    @PreAuthorize("hasAnyRole('OWNER')")
    @Operation(summary = "Update staff member data")
    public ResponseEntity<StaffMemberResponse> updateStaffMember(
            @PathVariable Long tenantId,
            @PathVariable Long staffId,
            @Valid @RequestBody UpdateStaffMemberRequest request) {
        return ResponseEntity.ok(staffMemberService.updateStaffMember(tenantId, staffId, request));
    }

    @DeleteMapping("/{staffId}")
    @PreAuthorize("hasAnyRole('OWNER')")
    @Operation(summary = "Delete staff member")
    public ResponseEntity<Void> deleteStaffMember(
            @PathVariable Long tenantId,
            @PathVariable Long staffId) {
        staffMemberService.deleteStaffMember(tenantId, staffId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync-from-user")
    @Operation(summary = "Sync staff member data with user profile")
    public ResponseEntity<Void> syncFromUser(@RequestBody SyncFromUserRequest request) {
        staffMemberService.syncFromUser(request.userId(), request.firstName(), request.lastName(), request.avatarUrl(), request.phone(), request.email());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{staffId}/working-hours")
    @PreAuthorize("hasAnyRole('OWNER', 'DENTIST', 'RECEPTIONIST', 'ASSISTANT', 'PATIENT')")
    @Operation(summary = "Get staff member working hours (per day of week)")
    public ResponseEntity<List<StaffWorkingHoursDTO>> getWorkingHours(
            @PathVariable Long tenantId,
            @PathVariable Long staffId) {
        return ResponseEntity.ok(workingHoursService.getWorkingHours(tenantId, staffId));
    }

    @PutMapping("/{staffId}/working-hours")
    @PreAuthorize("hasAnyRole('OWNER', 'DENTIST')")
    @Operation(summary = "Update staff member working hours (per day of week)")
    public ResponseEntity<Void> updateWorkingHours(
            @PathVariable Long tenantId,
            @PathVariable Long staffId,
            @RequestBody UpdateWorkingHoursRequest request) {
        workingHoursService.updateWorkingHours(tenantId, staffId, request);
        return ResponseEntity.ok().build();
    }
}
