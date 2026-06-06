package pl.edu.ur.dentflow.core.scheduling.api;

import pl.edu.ur.dentflow.core.scheduling.domain.WorkScheduleSlot;
import java.time.OffsetDateTime;

public record WorkScheduleSlotResponse(
        Long id,
        Long tenantId,
        Long staffId,
        Long locationId,
        Long roomId,
        OffsetDateTime startAt,
        OffsetDateTime endAt
) {
    public static WorkScheduleSlotResponse from(WorkScheduleSlot slot) {
        return new WorkScheduleSlotResponse(
                slot.getId(),
                slot.getTenantId(),
                slot.getStaffId(),
                slot.getLocationId(),
                slot.getRoomId(),
                slot.getStartAt(),
                slot.getEndAt()
        );
    }
}
