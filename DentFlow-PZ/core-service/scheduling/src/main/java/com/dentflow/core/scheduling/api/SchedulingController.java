package com.dentflow.core.scheduling.api;

import com.dentflow.core.scheduling.application.SchedulingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/tenants/{tenantId}/schedule")
public class SchedulingController {

    private final SchedulingService schedulingService;

    public SchedulingController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    // ── Slots ──────────────────────────────────────────────────────────────────

    @GetMapping("/slots")
    public ResponseEntity<List<WorkScheduleSlotResponse>> getSlots(
            @PathVariable Long tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ResponseEntity.ok(schedulingService.getSlots(tenantId, from, to));
    }

    @PostMapping("/slots")
    public ResponseEntity<WorkScheduleSlotResponse> addSlot(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateWorkScheduleSlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(schedulingService.addSlot(tenantId, request));
    }

    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<Void> deleteSlot(
            @PathVariable Long tenantId,
            @PathVariable Long slotId) {
        schedulingService.deleteSlot(tenantId, slotId);
        return ResponseEntity.noContent().build();
    }

    // ── Blockers ───────────────────────────────────────────────────────────────

    @GetMapping("/blockers")
    public ResponseEntity<List<BlockerResponse>> getBlockers(@PathVariable Long tenantId) {
        return ResponseEntity.ok(schedulingService.getBlockers(tenantId));
    }

    @PostMapping("/blockers")
    public ResponseEntity<BlockerResponse> addBlocker(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateBlockerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(schedulingService.addBlocker(tenantId, request));
    }

    @DeleteMapping("/blockers/{blockerId}")
    public ResponseEntity<Void> deleteBlocker(
            @PathVariable Long tenantId,
            @PathVariable Long blockerId) {
        schedulingService.deleteBlocker(tenantId, blockerId);
        return ResponseEntity.noContent().build();
    }
}
