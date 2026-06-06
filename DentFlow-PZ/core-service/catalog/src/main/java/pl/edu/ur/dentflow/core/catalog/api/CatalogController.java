package pl.edu.ur.dentflow.core.catalog.api;

import pl.edu.ur.dentflow.core.catalog.application.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenants/{tenantId}/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /**
     * GET /tenants/{tenantId}/catalog
     * Zwraca wszystkie usługi (aktywne i nieaktywne).
     * Opcjonalny parametr ?activeOnly=true zwraca tylko aktywne.
     */
    @GetMapping
    public ResponseEntity<List<ServiceCatalogItemDTO>> getServices(
            @PathVariable Long tenantId,
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        List<ServiceCatalogItemDTO> result = activeOnly
                ? catalogService.getActiveServices(tenantId)
                : catalogService.getAllServices(tenantId);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /tenants/{tenantId}/catalog/{id}
     * Zwraca pojedynczą usługę po id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceCatalogItemDTO> getService(
            @PathVariable Long tenantId,
            @PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getService(tenantId, id));
    }

    /**
     * POST /tenants/{tenantId}/catalog
     * Tworzy nową pozycję w cenniku.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DOCTOR')")
    public ResponseEntity<ServiceCatalogItemDTO> createService(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateServiceCatalogItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogService.createService(tenantId, request));
    }

    /**
     * PUT /tenants/{tenantId}/catalog/{id}
     * Aktualizuje istniejącą pozycję w cenniku.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DOCTOR')")
    public ResponseEntity<ServiceCatalogItemDTO> updateService(
            @PathVariable Long tenantId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceCatalogItemRequest request) {
        return ResponseEntity.ok(catalogService.updateService(tenantId, id, request));
    }

    /**
     * DELETE /tenants/{tenantId}/catalog/{id}
     * Usuwa pozycję z cennika.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DOCTOR')")
    public ResponseEntity<Void> deleteService(
            @PathVariable Long tenantId,
            @PathVariable Long id) {
        catalogService.deleteService(tenantId, id);
        return ResponseEntity.noContent().build();
    }
}
