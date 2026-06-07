package pl.edu.ur.dentflow.core.reservation.application;

import pl.edu.ur.dentflow.core.reservation.domain.Appointment;

public class AppointmentCompletedEvent {

    private final Long tenantId;
    private final Appointment appointment;

    public AppointmentCompletedEvent(Long tenantId, Appointment appointment) {
        this.tenantId = tenantId;
        this.appointment = appointment;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Appointment getAppointment() {
        return appointment;
    }
}
