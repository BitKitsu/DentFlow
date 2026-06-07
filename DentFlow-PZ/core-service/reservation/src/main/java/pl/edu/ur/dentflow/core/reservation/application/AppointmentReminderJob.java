package pl.edu.ur.dentflow.core.reservation.application;

import pl.edu.ur.dentflow.core.notification.application.EmailService;
import pl.edu.ur.dentflow.core.patient.domain.Patient;
import pl.edu.ur.dentflow.core.patient.infrastructure.PatientRepository;
import pl.edu.ur.dentflow.core.clinic.domain.StaffMember;
import pl.edu.ur.dentflow.core.clinic.infrastructure.StaffMemberRepository;
import pl.edu.ur.dentflow.core.reservation.domain.Appointment;
import pl.edu.ur.dentflow.core.reservation.infrastructure.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Cron job sending appointment reminders 24h before the scheduled time.
 * Runs daily at 8:00 AM and finds appointments scheduled
 * for the next day (from 8:00 to 8:00 + 24h).
 */
@Component
public class AppointmentReminderJob {

    private static final Logger log = LoggerFactory.getLogger(AppointmentReminderJob.class);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final StaffMemberRepository staffMemberRepository;
    private final EmailService emailService;

    public AppointmentReminderJob(AppointmentRepository appointmentRepository,
                                  PatientRepository patientRepository,
                                  StaffMemberRepository staffMemberRepository,
                                  EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.staffMemberRepository = staffMemberRepository;
        this.emailService = emailService;
    }

    /**
     * Runs daily at 8:00 AM UTC.
     * Sends reminders for appointments scheduled in 24h (±30 min window).
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendReminders() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime from = now.plusHours(23).plusMinutes(30);
        OffsetDateTime to   = now.plusHours(24).plusMinutes(30);

        List<Appointment> upcoming = appointmentRepository.findUpcomingScheduled(from, to);
        log.info("Reminder cron: found {} appointments to remind (window {} – {})",
                upcoming.size(), from, to);

        for (Appointment appointment : upcoming) {
            try {
                Patient patient = patientRepository
                        .findByIdAndTenantId(appointment.getPatientId(), appointment.getTenantId())
                        .orElse(null);
                StaffMember dentist = staffMemberRepository
                        .findByIdAndTenantId(appointment.getDentistStaffId(), appointment.getTenantId())
                        .orElse(null);

                if (patient == null || patient.getEmail() == null || patient.getEmail().isBlank()) {
                    log.debug("Appointment {}: no patient email, skipping.", appointment.getId());
                    continue;
                }

                String patientName = patient.getFirstName() + " " + patient.getLastName();
                String dentistName = dentist != null ? dentist.getFirstName() + " " + dentist.getLastName() : "Lekarz";

                emailService.sendAppointmentReminder(
                        patient.getEmail(),
                        patientName,
                        "DentFlow",
                        dentistName,
                        appointment.getStartAt(),
                        "Gabinet"
                );
                log.info("Reminder sent for appointment {} to {}", appointment.getId(), patient.getEmail());
            } catch (Exception e) {
                log.error("Error sending reminder for appointment {}: {}", appointment.getId(), e.getMessage());
            }
        }
    }
}
