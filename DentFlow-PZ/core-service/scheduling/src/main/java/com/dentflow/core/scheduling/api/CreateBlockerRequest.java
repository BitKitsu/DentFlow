package com.dentflow.core.scheduling.api;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CreateBlockerRequest(
        Long staffId,
        Long roomId,
        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt,
        String reason
) {}
