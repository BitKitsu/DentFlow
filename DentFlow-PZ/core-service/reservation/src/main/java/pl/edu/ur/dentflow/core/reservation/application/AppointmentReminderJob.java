package pl.edu.ur.dentflow.core.reservation.application;

import pl.edu.ur.dentflow.core.clinic.domain.StaffMember;
import pl.edu.ur.dentflow.core.clinic.infrastructure.StaffMemberRepository;
import pl.edu.ur.dentflow.core.notification.api.CreateNotificationRequest;
import pl.edu.ur.dentflow.core.notification.infrastructure.NotificationRepository;
import pl.edu.ur.dentflow.core.patient.domain.Patient;
import pl.edu.ur.dentflow.core.patient.infrastructure.PatientRepository;
import pl.edu.ur.dentflow.core.reservation.domain.Appointment;
import pl.edu.ur.dentflow.core.reservation.infrastructure.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class AppointmentReminderJob {

    private static final Logger log = LoggerFactory.getLogger(AppointmentReminderJob.class);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final StaffMemberRepository staffMemberRepository;
    private final NotificationRepository notificationRepository;

    public AppointmentReminderJob(AppointmentRepository appointmentRepository,
                                  PatientRepository patientRepository,
                                  StaffMemberRepository staffMemberRepository,
                                  NotificationRepository notificationRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.staffMemberRepository = staffMemberRepository;
        this.notificationRepository = notificationRepository;
    }

    @Scheduled(cron = "0 15 * * * *")
    public void sendReminders() {
        OffsetDateTime now = OffsetDateTime.now();

        sendRemindersForWindow(now, now.plusHours(23), now.plusHours(25), "24h");
        sendRemindersForWindow(now, now.plusHours(11), now.plusHours(13), "12h");

        log.info("Reminder cron: completed");
    }

    private void sendRemindersForWindow(OffsetDateTime from, OffsetDateTime to, OffsetDateTime windowEnd, String label) {
        var appointments = appointmentRepository.findUpcomingScheduled(from, to);
        log.info("Reminder cron ({}): found {} upcoming SCHEDULED appointments", label, appointments.size());

        for (Appointment appointment : appointments) {
            try {
                Patient patient = patientRepository.findByIdAndTenantId(
                        appointment.getPatientId(), appointment.getTenantId()).orElse(null);
                StaffMember dentist = staffMemberRepository.findByIdAndTenantId(
                        appointment.getDentistStaffId(), appointment.getTenantId()).orElse(null);

                String patientName = patient != null
                        ? patient.getFirstName() + " " + patient.getLastName() : "Patient";
                String dentistName = dentist != null
                        ? dentist.getFirstName() + " " + dentist.getLastName() : "Lekarz";

                String type = "APPOINTMENT_REMINDER_" + label;

                if (dentist != null && dentist.getUserId() != null) {
                    String msg = "Przypomnienie (" + label + "): wizyta z " + patientName + " – " + appointment.getStartAt();
                    if (!notificationRepository.existsByTenantIdAndUserIdAndTypeAndMessage(
                            appointment.getTenantId(), dentist.getUserId(), type, msg)) {
                        notificationRepository.save(
                                pl.edu.ur.dentflow.core.notification.domain.Notification.builder()
                                        .tenantId(appointment.getTenantId())
                                        .userId(dentist.getUserId())
                                        .type(type)
                                        .message(msg)
                                        .build());
                    }
                }

                if (patient != null && patient.getUserId() != null) {
                    String msg = "Przypomnienie (" + label + "): wizyta u " + dentistName + " – " + appointment.getStartAt();
                    if (!notificationRepository.existsByTenantIdAndUserIdAndTypeAndMessage(
                            appointment.getTenantId(), patient.getUserId(), type, msg)) {
                        notificationRepository.save(
                                pl.edu.ur.dentflow.core.notification.domain.Notification.builder()
                                        .tenantId(appointment.getTenantId())
                                        .userId(patient.getUserId())
                                        .type(type)
                                        .message(msg)
                                        .build());
                    }
                }
            } catch (Exception e) {
                log.error("Reminder cron: error processing appointment {}: {}", appointment.getId(), e.getMessage());
            }
        }
    }
}
