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

    @Transactional
    public AppointmentResponse confirmAppointment(Long tenantId, Long appointmentId) {
        Appointment appointment = findOrThrow(tenantId, appointmentId);
        if (!"SCHEDULED".equals(appointment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only SCHEDULED appointments can be confirmed");
        }
        appointment.setStatus("CONFIRMED");
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponse.from(saved);
    }

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
