package pl.edu.ur.dentflow.core.scheduling.application;

import pl.edu.ur.dentflow.core.scheduling.api.*;
import pl.edu.ur.dentflow.core.scheduling.domain.Blocker;
import pl.edu.ur.dentflow.core.scheduling.domain.WorkScheduleSlot;
import pl.edu.ur.dentflow.core.scheduling.infrastructure.BlockerRepository;
import pl.edu.ur.dentflow.core.scheduling.infrastructure.WorkScheduleSlotRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class SchedulingService {

    private final WorkScheduleSlotRepository slotRepository;
    private final BlockerRepository blockerRepository;

    public SchedulingService(WorkScheduleSlotRepository slotRepository,
                             BlockerRepository blockerRepository) {
        this.slotRepository = slotRepository;
        this.blockerRepository = blockerRepository;
    }

    // ── Slots ──────────────────────────────────────────────────────────────────

    public List<WorkScheduleSlotResponse> getSlots(Long tenantId,
                                                    OffsetDateTime from,
                                                    OffsetDateTime to) {
        List<WorkScheduleSlot> slots;
        if (from != null && to != null) {
            slots = slotRepository.findByTenantIdAndDateRange(tenantId, from, to);
        } else {
            slots = slotRepository.findByTenantId(tenantId);
        }
        return slots.stream().map(WorkScheduleSlotResponse::from).toList();
    }

    @Transactional
    public WorkScheduleSlotResponse addSlot(Long tenantId, CreateWorkScheduleSlotRequest request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endAt musi być po startAt");
        }
        List<WorkScheduleSlot> overlapping = slotRepository.findOverlapping(
                tenantId, request.staffId(), request.startAt(), request.endAt());
        if (!overlapping.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Slot koliduje z istniejącymi slotami tego pracownika");
        }
        WorkScheduleSlot slot = WorkScheduleSlot.builder()
                .tenantId(tenantId)
                .staffId(request.staffId())
                .locationId(request.locationId())
                .roomId(request.roomId())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .build();
        return WorkScheduleSlotResponse.from(slotRepository.save(slot));
    }

    @Transactional
    public WorkScheduleSlotResponse updateSlot(Long tenantId, Long slotId, UpdateWorkScheduleSlotRequest request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endAt musi być po startAt");
        }
        WorkScheduleSlot slot = slotRepository.findById(slotId)
                .filter(s -> s.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot nie istnieje"));
        List<WorkScheduleSlot> overlapping = slotRepository.findOverlapping(
                tenantId, slot.getStaffId(), request.startAt(), request.endAt());
        overlapping = overlapping.stream().filter(s -> !s.getId().equals(slotId)).toList();
        if (!overlapping.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Slot koliduje z istniejącymi slotami tego pracownika");
        }
        slot.setLocationId(request.locationId());
        slot.setRoomId(request.roomId());
        slot.setStartAt(request.startAt());
        slot.setEndAt(request.endAt());
        return WorkScheduleSlotResponse.from(slotRepository.save(slot));
    }

    @Transactional
    public void deleteSlot(Long tenantId, Long slotId) {
        WorkScheduleSlot slot = slotRepository.findById(slotId)
                .filter(s -> s.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot nie istnieje"));
        slotRepository.delete(slot);
    }

    // ── Blockers ───────────────────────────────────────────────────────────────

    public List<BlockerResponse> getBlockers(Long tenantId) {
        return blockerRepository.findByTenantId(tenantId)
                .stream().map(BlockerResponse::from).toList();
    }

    @Transactional
    public BlockerResponse addBlocker(Long tenantId, CreateBlockerRequest request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endAt musi być po startAt");
        }
        List<Blocker> conflicting = blockerRepository.findConflicting(
                tenantId, request.staffId(), request.roomId(), request.startAt(), request.endAt());
        if (!conflicting.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Blokada koliduje z istniejącymi blokadami");
        }
        Blocker blocker = Blocker.builder()
                .tenantId(tenantId)
                .staffId(request.staffId())
                .roomId(request.roomId())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .reason(request.reason())
                .build();
        return BlockerResponse.from(blockerRepository.save(blocker));
    }

    @Transactional
    public void deleteBlocker(Long tenantId, Long blockerId) {
        Blocker blocker = blockerRepository.findById(blockerId)
                .filter(b -> b.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blokada nie istnieje"));
        blockerRepository.delete(blocker);
    }
}
