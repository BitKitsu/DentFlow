package pl.edu.ur.dentflow.core.scheduling.api;

import pl.edu.ur.dentflow.core.scheduling.application.SchedulingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller managing schedule blockers in the DentFlow system.
 *
 * <p>Blockers represent time periods when a dentist is unavailable
 * (e.g., vacation, sick leave, lunch break). The system uses blockers
 * to prevent appointment booking during blocked periods.</p>
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /tenants/{tenantId}/schedule/blockers - list all blockers</li>
 *   <li>POST /tenants/{tenantId}/schedule/blockers - add a blocker (OWNER, DENTIST)</li>
 *   <li>DELETE /tenants/{tenantId}/schedule/blockers/{blockerId} - delete a blocker (OWNER, DENTIST)</li>
 * </ul>
 *
 * @see pl.edu.ur.dentflow.core.scheduling.application.SchedulingService
 */
@RestController
@RequestMapping("/tenants/{tenantId}/schedule")
public class SchedulingController {

    private final SchedulingService schedulingService;

    public SchedulingController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    // ── Blockers ───────────────────────────────────────────────────────────────

    @GetMapping("/blockers")
    public ResponseEntity<List<BlockerResponse>> getBlockers(@PathVariable Long tenantId) {
        return ResponseEntity.ok(schedulingService.getBlockers(tenantId));
    }

    @PostMapping("/blockers")
    @PreAuthorize("hasAnyRole('OWNER', 'DENTIST')")
    public ResponseEntity<BlockerResponse> addBlocker(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateBlockerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(schedulingService.addBlocker(tenantId, request));
    }

    @DeleteMapping("/blockers/{blockerId}")
    @PreAuthorize("hasAnyRole('OWNER', 'DENTIST')")
    public ResponseEntity<Void> deleteBlocker(
            @PathVariable Long tenantId,
            @PathVariable Long blockerId) {
        schedulingService.deleteBlocker(tenantId, blockerId);
        return ResponseEntity.noContent().build();
    }
}
