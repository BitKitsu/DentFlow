package com.dentflow.core.reservation.api;

import com.dentflow.core.reservation.domain.Appointment;
import java.time.OffsetDateTime;

public record AppointmentResponse(
        Long id,
        Long tenantId,
        Long locationId,
        Long roomId,
        Long dentistStaffId,
        Long patientId,
        Long serviceItemId,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String status,
        Long createdByUserId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String notes
) {
    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getTenantId(),
                a.getLocationId(),
                a.getRoomId(),
                a.getDentistStaffId(),
                a.getPatientId(),
                a.getServiceItemId(),
                a.getStartAt(),
                a.getEndAt(),
                a.getStatus(),
                a.getCreatedByUserId(),
                a.getCreatedAt(),
                a.getUpdatedAt(),
                a.getNotes()
        );
    }
}
