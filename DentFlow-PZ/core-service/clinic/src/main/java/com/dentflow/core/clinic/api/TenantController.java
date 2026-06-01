package com.dentflow.core.clinic.api;

import com.dentflow.core.clinic.application.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tenants")
@Tag(name = "Tenants", description = "Rejestracja i zarządzanie gabinetem (tenant)")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * POST /tenants/register
     * Rejestracja gabinetu - wywoływana zaraz po rejestracji OWNER w identity-service.
     * Publiczny endpoint (bez JWT), ponieważ jest wywoływany w toku rejestracji.
     */
    @PostMapping("/register")
    @Operation(summary = "Rejestracja gabinetu (tenant) z pierwszą lokalizacją")
    public ResponseEntity<TenantResponse> register(@Valid @RequestBody RegisterTenantRequest request) {
        TenantResponse response = tenantService.registerTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /tenants/{tenantId}
     * Pobranie danych gabinetu. Wymaga JWT.
     */
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
}
