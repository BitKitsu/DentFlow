package com.dentflow.core.reservation.api;

import java.time.OffsetDateTime;

/**
 * DTO reprezentujące pojedynczą wizytę w historii pacjenta.
 *
 * @param id              identyfikator wizyty
 * @param tenantId        identyfikator gabinetu
 * @param locationId      identyfikator lokalizacji
 * @param roomId          identyfikator pokoju
 * @param dentistStaffId  identyfikator dentysty
 * @param serviceItemId   identyfikator usługi
 * @param startAt         data i czas rozpoczęcia
 * @param endAt           data i czas zakończenia
 * @param status          status wizyty (SCHEDULED, COMPLETED, CANCELLED, NO_SHOW)
 * @param notes           notatki
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
