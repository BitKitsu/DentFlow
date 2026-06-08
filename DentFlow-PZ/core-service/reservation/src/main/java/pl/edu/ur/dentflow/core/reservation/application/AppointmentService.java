package pl.edu.ur.dentflow.core.reservation.application;

import pl.edu.ur.dentflow.core.reservation.api.AppointmentResponse;
import pl.edu.ur.dentflow.core.reservation.api.CreateAppointmentRequest;
import pl.edu.ur.dentflow.core.reservation.api.UpdateAppointmentRequest;
import pl.edu.ur.dentflow.core.reservation.domain.Appointment;
import pl.edu.ur.dentflow.core.reservation.infrastructure.AppointmentRepository;
import pl.edu.ur.dentflow.core.scheduling.infrastructure.BlockerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Service responsible for appointment lifecycle management.
 *
 * <p>Handles creation, updating, confirmation, cancellation and completion of
 * appointments. Enforces conflict detection using pessimistic locking and
 * blocker cross-checks. Publishes domain events for async notification delivery.</p>
 *
 * @see AppointmentCreatedEvent
 * @see AppointmentCancelledEvent
 * @see AppointmentCompletedEvent
 */
@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final BlockerRepository blockerRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              BlockerRepository blockerRepository,
                              ApplicationEventPublisher eventPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.blockerRepository = blockerRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Returns appointments for a tenant, optionally filtered by date range.
     *
     * @param tenantId the tenant (clinic) identifier
     * @param from     optional start of date range (inclusive)
     * @param to       optional end of date range (inclusive)
     * @return list of appointments ordered by start time descending
     */
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

    /**
     * Returns appointments created by a specific user (patient's own appointments).
     *
     * @param tenantId the tenant identifier
     * @param userId   the user who created the appointments
     * @return list of user's appointments ordered by start time descending
     */
    public List<AppointmentResponse> getMyAppointments(Long tenantId, Long userId) {
        return appointmentRepository
                .findByTenantIdAndCreatedByUserIdOrderByStartAtDesc(tenantId, userId)
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    /**
     * Returns a single appointment by ID.
     *
     * @param tenantId      the tenant identifier
     * @param appointmentId the appointment identifier
     * @return the appointment response
     * @throws ResponseStatusException 404 if not found
     */
    public AppointmentResponse getAppointment(Long tenantId, Long appointmentId) {
        return AppointmentResponse.from(findOrThrow(tenantId, appointmentId));
    }

    /**
     * Creates a new appointment with conflict and blocker validation.
     *
     * <p>Uses pessimistic locking ({@code SELECT FOR UPDATE}) to prevent race
     * conditions when multiple users book the same dentist slot simultaneously.
     * Also checks for overlapping blockers (vacations, maintenance).</p>
     *
     * @param tenantId the tenant identifier
     * @param request  the appointment creation payload
     * @return the created appointment with SCHEDULED status
     * @throws ResponseStatusException 400 if endAt is not after startAt
     * @throws ResponseStatusException 409 if dentist has a conflicting appointment or blocker
     */
    @Transactional
    public AppointmentResponse createAppointment(Long tenantId, CreateAppointmentRequest request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endAt must be after startAt");
        }

        List<Appointment> conflicts = appointmentRepository.findConflictingForUpdate(
                tenantId, request.dentistStaffId(), request.startAt(), request.endAt());
        if (!conflicts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Dentist already has an appointment at the given time");
        }

        List<?> blockerConflicts = blockerRepository.findConflicting(
                tenantId, request.dentistStaffId(), request.roomId(),
                request.startAt(), request.endAt());
        if (!blockerConflicts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Time slot is blocked (vacation/maintenance)");
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

        eventPublisher.publishEvent(new AppointmentCreatedEvent(tenantId, saved));

        return AppointmentResponse.from(saved);
    }

    /**
     * Updates an existing appointment's time, service, room and notes.
     *
     * <p>Re-checks conflict detection excluding the current appointment.</p>
     *
     * @param tenantId      the tenant identifier
     * @param appointmentId the appointment to update
     * @param request       the update payload
     * @return the updated appointment
     * @throws ResponseStatusException 404 if not found
     * @throws ResponseStatusException 409 if new time conflicts with another appointment
     */
    @Transactional
    public AppointmentResponse updateAppointment(Long tenantId, Long appointmentId,
                                                  UpdateAppointmentRequest request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endAt must be after startAt");
        }
        Appointment appointment = findOrThrow(tenantId, appointmentId);

        List<Appointment> conflicts = appointmentRepository.findConflictingForUpdate(
                tenantId, appointment.getDentistStaffId(), request.startAt(), request.endAt())
                .stream().filter(a -> !a.getId().equals(appointmentId)).toList();
        if (!conflicts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Dentist already has an appointment at the given time");
        }

        appointment.setStartAt(request.startAt());
        appointment.setEndAt(request.endAt());
        appointment.setServiceItemId(request.serviceItemId());
        appointment.setRoomId(request.roomId());
        appointment.setNotes(request.notes());

        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    /**
     * Confirms a SCHEDULED appointment (transitions to CONFIRMED status).
     *
     * @param tenantId      the tenant identifier
     * @param appointmentId the appointment to confirm
     * @return the confirmed appointment
     * @throws ResponseStatusException 400 if current status is not SCHEDULED
     */
    @Transactional
    public AppointmentResponse confirmAppointment(Long tenantId, Long appointmentId) {
        Appointment appointment = findOrThrow(tenantId, appointmentId);
        if (!"SCHEDULED".equals(appointment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only SCHEDULED appointments can be confirmed");
        }
        appointment.setStatus("CONFIRMED");
        Appointment saved = appointmentRepository.save(appointment);

        eventPublisher.publishEvent(new AppointmentConfirmedEvent(tenantId, saved));

        return AppointmentResponse.from(saved);
    }

    /**
     * Cancels an appointment (transitions to CANCELLED status).
     *
     * <p>Publishes {@link AppointmentCancelledEvent} for async notification delivery.</p>
     *
     * @param tenantId      the tenant identifier
     * @param appointmentId the appointment to cancel
     * @return the cancelled appointment
     * @throws ResponseStatusException 400 if already cancelled
     */
    @Transactional
    public AppointmentResponse cancelAppointment(Long tenantId, Long appointmentId) {
        Appointment appointment = findOrThrow(tenantId, appointmentId);
        if ("CANCELLED".equals(appointment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment is already cancelled");
        }
        appointment.setStatus("CANCELLED");
        Appointment saved = appointmentRepository.save(appointment);

        eventPublisher.publishEvent(new AppointmentCancelledEvent(tenantId, saved));

        return AppointmentResponse.from(saved);
    }

    /**
     * Marks an appointment as completed (transitions to COMPLETED status).
     *
     * <p>Publishes {@link AppointmentCompletedEvent} for async notification delivery.</p>
     *
     * @param tenantId      the tenant identifier
     * @param appointmentId the appointment to complete
     * @return the completed appointment
     */
    @Transactional
    public AppointmentResponse completeAppointment(Long tenantId, Long appointmentId) {
        Appointment appointment = findOrThrow(tenantId, appointmentId);
        appointment.setStatus("COMPLETED");
        Appointment saved = appointmentRepository.save(appointment);

        eventPublisher.publishEvent(new AppointmentCompletedEvent(tenantId, saved));

        return AppointmentResponse.from(saved);
    }

    private Appointment findOrThrow(Long tenantId, Long appointmentId) {
        return appointmentRepository.findByIdAndTenantId(appointmentId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Appointment does not exist"));
    }
}
