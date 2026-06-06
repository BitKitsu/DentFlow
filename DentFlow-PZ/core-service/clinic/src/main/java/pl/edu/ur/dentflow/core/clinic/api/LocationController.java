package pl.edu.ur.dentflow.core.clinic.api;

import pl.edu.ur.dentflow.core.clinic.application.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenants/{tenantId}/locations")
@Tag(name = "Locations", description = "Zarządzanie lokalizacjami gabinetu (stub)")
@SecurityRequirement(name = "bearerAuth")
public class LocationController {

    private final TenantService tenantService;

    public LocationController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    @Operation(summary = "[stub] Lista lokalizacji gabinetu")
    public ResponseEntity<List<LocationResponse>> getLocations(@PathVariable Long tenantId) {
        return ResponseEntity.ok(tenantService.getLocations(tenantId));
    }

    @PostMapping
    @Operation(summary = "[stub] Dodanie lokalizacji do gabinetu")
    public ResponseEntity<LocationResponse> addLocation(
            @PathVariable Long tenantId,
            @Valid @RequestBody AddLocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tenantService.addLocation(tenantId, request));
    }

    @GetMapping("/{locationId}")
    @Operation(summary = "Pobranie lokalizacji")
    public ResponseEntity<LocationResponse> getLocation(
            @PathVariable Long tenantId,
            @PathVariable Long locationId) {
        return ResponseEntity.ok(tenantService.getLocation(tenantId, locationId));
    }

    @PutMapping("/{locationId}")
    @Operation(summary = "Zaktualizowanie lokalizacji")
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable Long tenantId,
            @PathVariable Long locationId,
            @Valid @RequestBody UpdateLocationRequest request) {
        return ResponseEntity.ok(tenantService.updateLocation(tenantId, locationId, request));
    }

    @DeleteMapping("/{locationId}")
    @Operation(summary = "[stub] Usunięcie lokalizacji")
    public ResponseEntity<Void> deleteLocation(
            @PathVariable Long tenantId,
            @PathVariable Long locationId) {
        tenantService.deleteLocation(tenantId, locationId);
        return ResponseEntity.noContent().build();
    }
}
