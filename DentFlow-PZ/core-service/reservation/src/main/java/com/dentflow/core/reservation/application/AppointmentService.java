package com.dentflow.core.reservation.application;

import com.dentflow.core.reservation.api.AppointmentResponse;
import com.dentflow.core.reservation.api.CreateAppointmentRequest;
import com.dentflow.core.reservation.api.UpdateAppointmentRequest;
import com.dentflow.core.reservation.domain.Appointment;
import com.dentflow.core.reservation.infrastructure.AppointmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
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

        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse updateAppointment(Long tenantId, Long appointmentId,
                                                  UpdateAppointmentRequest request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endAt musi być po startAt");
        }
        Appointment appointment = findOrThrow(tenantId, appointmentId);

        // Sprawdzenie konfliktu – wykluczamy samą wizytę
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
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse completeAppointment(Long tenantId, Long appointmentId) {
        Appointment appointment = findOrThrow(tenantId, appointmentId);
        appointment.setStatus("COMPLETED");
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    private Appointment findOrThrow(Long tenantId, Long appointmentId) {
        return appointmentRepository.findByIdAndTenantId(appointmentId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Wizyta nie istnieje"));
    }
}
