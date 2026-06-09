package pl.edu.ur.dentflow.core.reservation.application;

import pl.edu.ur.dentflow.core.reservation.domain.Appointment;
import pl.edu.ur.dentflow.core.reservation.infrastructure.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Cron job that auto-completes past CONFIRMED appointments.
 * Runs every hour. Finds appointments that are still CONFIRMED
 * but whose endAt is already in the past.
 */
@Component
public class AppointmentAutoCompleteJob {

    private static final Logger log = LoggerFactory.getLogger(AppointmentAutoCompleteJob.class);

    private final AppointmentRepository appointmentRepository;

    public AppointmentAutoCompleteJob(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Scheduled(cron = "0 5 * * * *")
    @Transactional
    public void autoCompletePastConfirmed() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Appointment> pastConfirmed = appointmentRepository.findPastConfirmed(now);

        log.info("AutoComplete cron: found {} past CONFIRMED appointments to mark as COMPLETED", pastConfirmed.size());

        for (Appointment appointment : pastConfirmed) {
            appointment.setStatus("COMPLETED");
            appointmentRepository.save(appointment);
            log.info("Appointment {} (tenant {}) auto-completed", appointment.getId(), appointment.getTenantId());
        }
    }
}
