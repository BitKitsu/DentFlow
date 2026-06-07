package pl.edu.ur.dentflow.core.scheduling.api;

import pl.edu.ur.dentflow.core.scheduling.application.SchedulingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
