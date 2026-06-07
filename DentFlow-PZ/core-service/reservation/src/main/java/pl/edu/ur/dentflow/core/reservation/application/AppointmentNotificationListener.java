package pl.edu.ur.dentflow.core.reservation.application;

import pl.edu.ur.dentflow.core.clinic.domain.StaffMember;
import pl.edu.ur.dentflow.core.clinic.infrastructure.StaffMemberRepository;
import pl.edu.ur.dentflow.core.notification.api.CreateNotificationRequest;
import pl.edu.ur.dentflow.core.notification.application.EmailService;
import pl.edu.ur.dentflow.core.notification.application.NotificationService;
import pl.edu.ur.dentflow.core.patient.domain.Patient;
import pl.edu.ur.dentflow.core.patient.infrastructure.PatientRepository;
import pl.edu.ur.dentflow.core.reservation.domain.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AppointmentNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(AppointmentNotificationListener.class);

    private final PatientRepository patientRepository;
    private final StaffMemberRepository staffMemberRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public AppointmentNotificationListener(PatientRepository patientRepository,
                                           StaffMemberRepository staffMemberRepository,
                                           EmailService emailService,
                                           NotificationService notificationService) {
        this.patientRepository = patientRepository;
        this.staffMemberRepository = staffMemberRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    @Async
    @EventListener
    public void handleAppointmentCreated(AppointmentCreatedEvent event) {
        sendCreatedNotifications(event.getTenantId(), event.getAppointment());
    }

    @Async
    @EventListener
    public void handleAppointmentCancelled(AppointmentCancelledEvent event) {
        sendCancelledNotifications(event.getTenantId(), event.getAppointment());
    }

    @Async
    @EventListener
    public void handleAppointmentCompleted(AppointmentCompletedEvent event) {
        sendCompletedNotifications(event.getTenantId(), event.getAppointment());
    }

    private void sendCreatedNotifications(Long tenantId, Appointment appointment) {
        try {
            Patient patient = patientRepository.findByIdAndTenantId(
                    appointment.getPatientId(), tenantId).orElse(null);
            StaffMember dentist = staffMemberRepository.findByIdAndTenantId(
                    appointment.getDentistStaffId(), tenantId).orElse(null);

            String patientName = patient != null
                    ? patient.getFirstName() + " " + patient.getLastName() : "Patient";
            String dentistName = dentist != null ? dentist.getFirstName() + " " + dentist.getLastName() : "Lekarz";

            if (patient != null && patient.getEmail() != null && !patient.getEmail().isBlank()) {
                emailService.sendAppointmentConfirmation(
                        patient.getEmail(), patientName, "DentFlow",
                        dentistName, appointment.getStartAt(), "Gabinet");
            }

            if (dentist != null && dentist.getUserId() != null) {
                notificationService.createNotification(tenantId, new CreateNotificationRequest(
                        dentist.getUserId(),
                        "APPOINTMENT",
                        "New appointment: " + patientName + " – " + appointment.getStartAt()
                ));
            }
        } catch (Exception e) {
            log.error("Error sending notifications after creating appointment {}: {}", appointment.getId(), e.getMessage());
        }
    }

    private void sendCancelledNotifications(Long tenantId, Appointment appointment) {
        try {
            Patient patient = patientRepository.findByIdAndTenantId(
                    appointment.getPatientId(), tenantId).orElse(null);
            StaffMember dentist = staffMemberRepository.findByIdAndTenantId(
                    appointment.getDentistStaffId(), tenantId).orElse(null);

            String patientName = patient != null
                    ? patient.getFirstName() + " " + patient.getLastName() : "Patient";

            if (patient != null && patient.getEmail() != null && !patient.getEmail().isBlank()) {
                emailService.sendAppointmentCancellation(
                        patient.getEmail(), patientName, "DentFlow", appointment.getStartAt());
            }

            if (dentist != null && dentist.getUserId() != null) {
                notificationService.createNotification(tenantId, new CreateNotificationRequest(
                        dentist.getUserId(),
                        "APPOINTMENT",
                        "Appointment cancelled: " + patientName + " – " + appointment.getStartAt()
                ));
            }
        } catch (Exception e) {
            log.error("Error sending notifications after cancelling appointment {}: {}", appointment.getId(), e.getMessage());
        }
    }

    private void sendCompletedNotifications(Long tenantId, Appointment appointment) {
        try {
            Patient patient = patientRepository.findByIdAndTenantId(
                    appointment.getPatientId(), tenantId).orElse(null);
            StaffMember dentist = staffMemberRepository.findByIdAndTenantId(
                    appointment.getDentistStaffId(), tenantId).orElse(null);

            String patientName = patient != null
                    ? patient.getFirstName() + " " + patient.getLastName() : "Patient";

            if (dentist != null && dentist.getUserId() != null) {
                notificationService.createNotification(tenantId, new CreateNotificationRequest(
                        dentist.getUserId(),
                        "APPOINTMENT",
                        "Appointment completed: " + patientName
                ));
            }
        } catch (Exception e) {
            log.error("Error sending notifications after completing appointment {}: {}", appointment.getId(), e.getMessage());
        }
    }
}
