package com.dentflow.core.reservation.api;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record UpdateAppointmentRequest(
        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt,
        Long serviceItemId,
        Long roomId,
        String notes
) {}
