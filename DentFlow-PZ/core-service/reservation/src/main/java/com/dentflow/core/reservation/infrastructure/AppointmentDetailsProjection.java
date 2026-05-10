package com.dentflow.core.reservation.infrastructure;

import java.time.OffsetDateTime;

public interface AppointmentDetailsProjection {
    Long getAppointmentId();
    OffsetDateTime getStartAt();
    OffsetDateTime getEndAt();
    String getStatus();
    String getPatientFirstName();
    String getPatientLastName();
    String getDentistName();
    String getLocationName();
    String getRoomName();
    String getNotes();
}
