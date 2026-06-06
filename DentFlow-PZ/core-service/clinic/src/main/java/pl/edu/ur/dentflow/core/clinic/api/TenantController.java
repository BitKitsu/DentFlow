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
    @Operation(summary = "Rejestracja gabinetu (tenant) z pierwszą lokalizacją")
    public ResponseEntity<TenantResponse> register(@Valid @RequestBody RegisterTenantRequest request) {
        TenantResponse response = tenantService.registerTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Lista wszystkich gabinetów")
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @GetMapping("/{tenantId}")
    @Operation(summary = "Pobranie danych gabinetu")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TenantResponse> getTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(tenantService.getTenant(tenantId));
    }

    @PutMapping("/{tenantId}")
    @Operation(summary = "Aktualizacja danych gabinetu")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TenantResponse> updateTenant(
            @PathVariable Long tenantId,
            @Valid @RequestBody UpdateTenantRequest request) {
        return ResponseEntity.ok(tenantService.updateTenant(tenantId, request));
    }

    @DeleteMapping("/{tenantId}")
    @Operation(summary = "Usunięcie gabinetu")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long tenantId) {
        tenantService.deleteTenant(tenantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/catalog/all")
    @Operation(summary = "Wszystkie aktywne usługi ze wszystkich gabinetów")
    public ResponseEntity<List<ServiceCatalogItemDTO>> getAllActiveCatalog() {
        return ResponseEntity.ok(catalogService.getAllActiveServices());
    }

    @GetMapping("/staff/all")
    @Operation(summary = "Wszyscy pracownicy ze wszystkich gabinetów")
    public ResponseEntity<List<StaffMemberResponse>> getAllStaffMembers() {
        return ResponseEntity.ok(staffMemberService.getAllStaffMembers());
    }
}
