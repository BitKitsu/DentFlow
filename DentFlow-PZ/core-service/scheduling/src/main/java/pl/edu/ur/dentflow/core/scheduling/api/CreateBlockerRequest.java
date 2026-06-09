package pl.edu.ur.dentflow.core.scheduling.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record CreateBlockerRequest(
        Long staffId,
        Long roomId,
        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt,
        @Size(max = 255) String reason
) {}
