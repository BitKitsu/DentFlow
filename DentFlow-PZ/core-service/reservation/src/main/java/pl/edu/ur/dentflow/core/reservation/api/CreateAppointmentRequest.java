package pl.edu.ur.dentflow.core.reservation.api;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CreateAppointmentRequest(
        @NotNull Long locationId,
        Long roomId,
        @NotNull Long dentistStaffId,
        Long patientId,
        Long serviceItemId,
        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt,
        Long createdByUserId,
        String notes
) {}
