package com.dentflow.core.reservation.application;

import com.dentflow.core.clinic.domain.StaffMember;
import com.dentflow.core.clinic.infrastructure.StaffMemberRepository;
import com.dentflow.core.notification.api.CreateNotificationRequest;
import com.dentflow.core.notification.application.EmailService;
import com.dentflow.core.notification.application.NotificationService;
import com.dentflow.core.patient.domain.Patient;
import com.dentflow.core.patient.infrastructure.PatientRepository;
import com.dentflow.core.reservation.api.AppointmentResponse;
import com.dentflow.core.reservation.api.CreateAppointmentRequest;
import com.dentflow.core.reservation.api.UpdateAppointmentRequest;
import com.dentflow.core.reservation.domain.Appointment;
import com.dentflow.core.reservation.infrastructure.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final StaffMemberRepository staffMemberRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              StaffMemberRepository staffMemberRepository,
                              EmailService emailService,
                              NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.staffMemberRepository = staffMemberRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    public List<AppointmentResponse> getAppointments(Long tenantId,
                                                      OffsetDateTime from,
                                                      OffsetDateTime to) {
        List<Appointment> result;
        if (from != null && to != null) {
            result = appointmentRepository.findByTenantIdAndDateRange(tenantId, from, to);
        } else {
            result = appointmentRepository.findByTenantId(tenantId);
        }
        return result.stream().map(AppointmentResponse::from).toList();
    }

    public List<AppointmentResponse> getMyAppointments(Long tenantId, Long userId) {
        return appointmentRepository
                .findByTenantIdAndCreatedByUserIdOrderByStartAtDesc(tenantId, userId)
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    public AppointmentResponse getAppointment(Long tenantId, Long appointmentId) {
        return AppointmentResponse.from(findOrThrow(tenantId, appointmentId));
    }

    @Transactional
    public AppointmentResponse createAppointment(Long tenantId, CreateAppointmentRequest request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endAt musi być po startAt");
        }

        List<Appointment> conflicts = appointmentRepository.findConflicting(
                tenantId, request.dentistStaffId(), request.startAt(), request.endAt());
        if (!conflicts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Dentysta ma już wizytę w podanym terminie");
        }

        Appointment appointment = Appointment.builder()
                .tenantId(tenantId)
                .locationId(request.locationId())
                .roomId(request.roomId())
                .dentistStaffId(request.dentistStaffId())
                .patientId(request.patientId())
                .serviceItemId(request.serviceItemId())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .status("SCHEDULED")
                .createdByUserId(request.createdByUserId())
                .notes(request.notes())
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        // Powiadomienia po zapisaniu
        sendCreatedNotifications(tenantId, saved);

        return AppointmentResponse.from(saved);
    }

    @Transactional
    public AppointmentResponse updateAppointment(Long tenantId, Long appointmentId,
                                                  UpdateAppointmentRequest request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endAt musi być po startAt");
        }
        Appointment appointment = findOrThrow(tenantId, appointmentId);

        List<Appointment> conflicts = appointmentRepository.findConflicting(
                tenantId, appointment.getDentistStaffId(), request.startAt(), request.endAt())
                .stream().filter(a -> !a.getId().equals(appointmentId)).toList();
        if (!conflicts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Dentysta ma już wizytę w podanym terminie");
        }

        appointment.setStartAt(request.startAt());
        appointment.setEndAt(request.endAt());
        appointment.setServiceItemId(request.serviceItemId());
        appointment.setRoomId(request.roomId());
        appointment.setNotes(request.notes());

        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long tenantId, Long appointmentId) {
        Appointment appointment = findOrThrow(tenantId, appointmentId);
        if ("CANCELLED".equals(appointment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wizyta jest już anulowana");
        }
        appointment.setStatus("CANCELLED");
        Appointment saved = appointmentRepository.save(appointment);

        // Powiadomienia po anulowaniu
        sendCancelledNotifications(tenantId, saved);

        return AppointmentResponse.from(saved);
    }

    @Transactional
    public AppointmentResponse completeAppointment(Long tenantId, Long appointmentId) {
        Appointment appointment = findOrThrow(tenantId, appointmentId);
        appointment.setStatus("COMPLETED");
        Appointment saved = appointmentRepository.save(appointment);

        // Powiadomienie in-app dla lekarza o zakończonej wizycie
        sendCompletedNotifications(tenantId, saved);

        return AppointmentResponse.from(saved);
    }

    // ── helpers – powiadomienia ───────────────────────────────────────────────

    private void sendCreatedNotifications(Long tenantId, Appointment appointment) {
        try {
            Patient patient = patientRepository.findByIdAndTenantId(
                    appointment.getPatientId(), tenantId).orElse(null);
            StaffMember dentist = staffMemberRepository.findByIdAndTenantId(
                    appointment.getDentistStaffId(), tenantId).orElse(null);

            String patientName = patient != null
                    ? patient.getFirstName() + " " + patient.getLastName() : "Pacjent";
            String dentistName = dentist != null ? dentist.getFirstName() + " " + dentist.getLastName() : "Lekarz";

            // Email do pacjenta
            if (patient != null && patient.getEmail() != null && !patient.getEmail().isBlank()) {
                emailService.sendAppointmentConfirmation(
                        patient.getEmail(), patientName, "DentFlow",
                        dentistName, appointment.getStartAt(), "Gabinet");
            }

            // Powiadomienie in-app dla lekarza (jeśli ma user_id)
            if (dentist != null && dentist.getUserId() != null) {
                notificationService.createNotification(tenantId, new CreateNotificationRequest(
                        dentist.getUserId(),
                        "APPOINTMENT",
                        "Nowa wizyta: " + patientName + " – " + appointment.getStartAt()
                ));
            }
        } catch (Exception e) {
            log.error("Błąd wysyłania powiadomień po utworzeniu wizyty {}: {}", appointment.getId(), e.getMessage());
        }
    }

    private void sendCancelledNotifications(Long tenantId, Appointment appointment) {
        try {
            Patient patient = patientRepository.findByIdAndTenantId(
                    appointment.getPatientId(), tenantId).orElse(null);
            StaffMember dentist = staffMemberRepository.findByIdAndTenantId(
                    appointment.getDentistStaffId(), tenantId).orElse(null);

            String patientName = patient != null
                    ? patient.getFirstName() + " " + patient.getLastName() : "Pacjent";

            // Email do pacjenta o anulowaniu
            if (patient != null && patient.getEmail() != null && !patient.getEmail().isBlank()) {
                emailService.sendAppointmentCancellation(
                        patient.getEmail(), patientName, "DentFlow", appointment.getStartAt());
            }

            // Powiadomienie in-app dla lekarza
            if (dentist != null && dentist.getUserId() != null) {
                notificationService.createNotification(tenantId, new CreateNotificationRequest(
                        dentist.getUserId(),
                        "APPOINTMENT",
                        "Wizyta anulowana: " + patientName + " – " + appointment.getStartAt()
                ));
            }
        } catch (Exception e) {
            log.error("Błąd wysyłania powiadomień po anulowaniu wizyty {}: {}", appointment.getId(), e.getMessage());
        }
    }

    private void sendCompletedNotifications(Long tenantId, Appointment appointment) {
        try {
            Patient patient = patientRepository.findByIdAndTenantId(
                    appointment.getPatientId(), tenantId).orElse(null);
            StaffMember dentist = staffMemberRepository.findByIdAndTenantId(
                    appointment.getDentistStaffId(), tenantId).orElse(null);

            String patientName = patient != null
                    ? patient.getFirstName() + " " + patient.getLastName() : "Pacjent";

            // Powiadomienie in-app dla lekarza
            if (dentist != null && dentist.getUserId() != null) {
                notificationService.createNotification(tenantId, new CreateNotificationRequest(
                        dentist.getUserId(),
                        "APPOINTMENT",
                        "Wizyta zakończona: " + patientName
                ));
            }
        } catch (Exception e) {
            log.error("Błąd wysyłania powiadomień po zakończeniu wizyty {}: {}", appointment.getId(), e.getMessage());
        }
    }

    private Appointment findOrThrow(Long tenantId, Long appointmentId) {
        return appointmentRepository.findByIdAndTenantId(appointmentId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Wizyta nie istnieje"));
    }
}
