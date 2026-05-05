package com.dentflow.core.scheduling.api;

import com.dentflow.core.scheduling.domain.Blocker;
import java.time.OffsetDateTime;

public record BlockerResponse(
        Long id,
        Long tenantId,
        Long staffId,
        Long roomId,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String reason
) {
    public static BlockerResponse from(Blocker blocker) {
        return new BlockerResponse(
                blocker.getId(),
                blocker.getTenantId(),
                blocker.getStaffId(),
                blocker.getRoomId(),
                blocker.getStartAt(),
                blocker.getEndAt(),
                blocker.getReason()
        );
    }
}
