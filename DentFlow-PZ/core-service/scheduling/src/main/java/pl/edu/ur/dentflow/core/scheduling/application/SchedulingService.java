package pl.edu.ur.dentflow.core.scheduling.application;

import pl.edu.ur.dentflow.core.scheduling.api.*;
import pl.edu.ur.dentflow.core.scheduling.domain.Blocker;
import pl.edu.ur.dentflow.core.scheduling.infrastructure.BlockerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service managing time blockers in the DentFlow system.
 *
 * <p><b>Blockers (Blocker):</b> Mark staff or room unavailability
 * (e.g., vacations, technical inspections). Blockers can apply to a staff member,
 * a room, or both. Validation detects conflicts with existing blockers.</p>
 *
 * @see pl.edu.ur.dentflow.core.scheduling.domain.Blocker
 */
@Service
public class SchedulingService {

    private final BlockerRepository blockerRepository;

    public SchedulingService(BlockerRepository blockerRepository) {
        this.blockerRepository = blockerRepository;
    }

    // ── Blockers ───────────────────────────────────────────────────────────────

    public List<BlockerResponse> getBlockers(Long tenantId) {
        return blockerRepository.findByTenantId(tenantId)
                .stream().map(BlockerResponse::from).toList();
    }

    @Transactional
    public BlockerResponse addBlocker(Long tenantId, CreateBlockerRequest request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endAt must be after startAt");
        }
        List<Blocker> conflicting = blockerRepository.findConflicting(
                tenantId, request.staffId(), request.roomId(), request.startAt(), request.endAt());
        if (!conflicting.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Blocker conflicts with existing blockers");
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blocker does not exist"));
        blockerRepository.delete(blocker);
    }
}
