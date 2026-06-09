package pl.edu.ur.dentflow.core.clinic.api;

import pl.edu.ur.dentflow.core.catalog.application.CatalogService;
import pl.edu.ur.dentflow.core.catalog.api.ServiceCatalogItemDTO;
import pl.edu.ur.dentflow.core.clinic.application.StaffMemberService;
import pl.edu.ur.dentflow.core.clinic.application.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller managing clinics (tenants) in the DentFlow system.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /tenants/register - register new clinic (public)</li>
 *   <li>GET /tenants - list all clinics (public - marketplace)</li>
 *   <li>GET /tenants/{tenantId} - clinic details</li>
 *   <li>PUT /tenants/{tenantId} - update clinic data</li>
 *   <li>DELETE /tenants/{tenantId} - delete clinic</li>
 *   <li>GET /tenants/catalog/all - public service catalog for all clinics</li>
 *   <li>GET /tenants/staff/all - public staff list for all clinics</li>
 * </ul>
 *
 * @see pl.edu.ur.dentflow.core.clinic.application.TenantService
 */
@RestController
@RequestMapping("/tenants")
public class TenantController {

    private final TenantService tenantService;
    private final CatalogService catalogService;
    private final StaffMemberService staffMemberService;

    public TenantController(TenantService tenantService, CatalogService catalogService, StaffMemberService staffMemberService) {
        this.tenantService = tenantService;
        this.catalogService = catalogService;
        this.staffMemberService = staffMemberService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register clinic (tenant) with first location")
    public ResponseEntity<TenantResponse> register(@Valid @RequestBody RegisterTenantRequest request) {
        TenantResponse response = tenantService.registerTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all clinics")
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @GetMapping("/{tenantId}")
    @Operation(summary = "Get clinic data")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TenantResponse> getTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(tenantService.getTenant(tenantId));
    }

    @PutMapping("/{tenantId}")
    @Operation(summary = "Update clinic data")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TenantResponse> updateTenant(
            @PathVariable Long tenantId,
            @Valid @RequestBody UpdateTenantRequest request) {
        return ResponseEntity.ok(tenantService.updateTenant(tenantId, request));
    }

    @DeleteMapping("/{tenantId}")
    @Operation(summary = "Delete clinic")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long tenantId) {
        tenantService.deleteTenant(tenantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/catalog/all")
    @Operation(summary = "All active services from all clinics")
    public ResponseEntity<List<ServiceCatalogItemDTO>> getAllActiveCatalog() {
        return ResponseEntity.ok(catalogService.getAllActiveServices());
    }

    @GetMapping("/staff/all")
    @Operation(summary = "All staff members from all clinics")
    public ResponseEntity<List<StaffMemberResponse>> getAllStaffMembers() {
        return ResponseEntity.ok(staffMemberService.getAllStaffMembers());
    }
}
