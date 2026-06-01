package com.dentflow.core.reservation.application;

import com.dentflow.core.notification.application.EmailService;
import com.dentflow.core.patient.domain.Patient;
import com.dentflow.core.patient.infrastructure.PatientRepository;
import com.dentflow.core.clinic.domain.StaffMember;
import com.dentflow.core.clinic.infrastructure.StaffMemberRepository;
import com.dentflow.core.reservation.domain.Appointment;
import com.dentflow.core.reservation.infrastructure.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Cron job wysyłający przypomnienia o wizytach 24h przed terminem.
 * Uruchamia się codziennie o 8:00 i szuka wizyt zaplanowanych
 * na następny dzień (od 8:00 do 8:00 + 24h).
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
     * Uruchamia się codziennie o 8:00 UTC.
     * Wysyła przypomnienia o wizytach zaplanowanych na za 24h (±30 min okno).
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendReminders() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime from = now.plusHours(23).plusMinutes(30);
        OffsetDateTime to   = now.plusHours(24).plusMinutes(30);

        List<Appointment> upcoming = appointmentRepository.findUpcomingScheduled(from, to);
        log.info("Cron przypomnienia: znaleziono {} wizyt do przypomnienia (okno {} – {})",
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
                    log.debug("Wizyta {}: brak emaila pacjenta, pomijam.", appointment.getId());
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
                log.info("Wysłano przypomnienie dla wizyty {} do {}", appointment.getId(), patient.getEmail());
            } catch (Exception e) {
                log.error("Błąd wysyłania przypomnienia dla wizyty {}: {}", appointment.getId(), e.getMessage());
            }
        }
    }
}
