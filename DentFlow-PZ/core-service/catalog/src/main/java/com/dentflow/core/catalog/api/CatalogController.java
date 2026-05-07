package com.dentflow.core.catalog.api;

import com.dentflow.core.catalog.application.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenants/{tenantId}/catalog")
@Tag(name = "Catalog", description = "Zarządzanie katalogiem usług (cennikiem)")
@SecurityRequirement(name = "bearerAuth")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    @Operation(summary = "Pobranie katalogu usług dla gabinetu")
    public ResponseEntity<List<ServiceCatalogItemDTO>> getServices(@PathVariable Long tenantId) {
        return ResponseEntity.ok(catalogService.getServices(tenantId));
    }
}
