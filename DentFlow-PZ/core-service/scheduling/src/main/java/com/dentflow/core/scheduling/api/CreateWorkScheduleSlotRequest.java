package com.dentflow.core.scheduling.api;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CreateWorkScheduleSlotRequest(
        @NotNull Long staffId,
        @NotNull Long locationId,
        Long roomId,
        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt
) {}
