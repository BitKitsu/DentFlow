package pl.edu.ur.dentflow.core.reservation.application;

import pl.edu.ur.dentflow.core.reservation.domain.Appointment;
import pl.edu.ur.dentflow.core.reservation.infrastructure.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Cron job that marks past SCHEDULED appointments as NO_SHOW.
 * Runs every hour. Finds appointments that are still SCHEDULED
 * but whose endAt is already in the past.
 */
@Component
public class AppointmentNoShowJob {

    private static final Logger log = LoggerFactory.getLogger(AppointmentNoShowJob.class);

    private final AppointmentRepository appointmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AppointmentNoShowJob(AppointmentRepository appointmentRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void markNoShows() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Appointment> pastScheduled = appointmentRepository.findPastScheduled(now);

        log.info("NoShow cron: found {} past SCHEDULED appointments to mark as NO_SHOW", pastScheduled.size());

        for (Appointment appointment : pastScheduled) {
            appointment.setStatus("NO_SHOW");
            Appointment saved = appointmentRepository.save(appointment);
            eventPublisher.publishEvent(new AppointmentNoShowEvent(appointment.getTenantId(), saved));
            log.info("Appointment {} (tenant {}) marked as NO_SHOW", appointment.getId(), appointment.getTenantId());
        }
    }
}
