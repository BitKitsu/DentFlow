package pl.edu.ur.dentflow.core.reservation.api;

import java.time.OffsetDateTime;

/**
 * DTO representing a single visit in patient history.
 *
 * @param id              visit identifier
 * @param tenantId        clinic identifier
 * @param locationId      location identifier
 * @param roomId          room identifier
 * @param dentistStaffId  dentist identifier
 * @param serviceItemId   service identifier
 * @param startAt         start date and time
 * @param endAt           end date and time
 * @param status          visit status (SCHEDULED, COMPLETED, CANCELLED, NO_SHOW)
 * @param notes           notes
 */
public record PatientVisitHistoryDTO(
        Long id,
        Long tenantId,
        Long locationId,
        Long roomId,
        Long dentistStaffId,
        Long serviceItemId,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String status,
        String notes
) {}
