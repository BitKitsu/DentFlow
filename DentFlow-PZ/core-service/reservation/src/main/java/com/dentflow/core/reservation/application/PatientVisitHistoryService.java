package com.dentflow.core.reservation.application;

import com.dentflow.core.reservation.api.PatientVisitHistoryDTO;
import com.dentflow.core.reservation.domain.Appointment;
import com.dentflow.core.reservation.infrastructure.AppointmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PatientVisitHistoryService {

    private final AppointmentRepository appointmentRepository;

    public PatientVisitHistoryService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Zwraca pełną historię wizyt pacjenta w ramach danego gabinetu (tenanta),
     * posortowaną od najnowszej wizyty.
     */
    @Transactional(readOnly = true)
    public List<PatientVisitHistoryDTO> getPatientHistory(Long tenantId, Long patientId) {
        List<Appointment> appointments =
                appointmentRepository.findByTenantIdAndPatientIdOrderByStartAtDesc(tenantId, patientId);

        if (appointments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Brak historii wizyt dla pacjenta o id=" + patientId + " w tym gabinecie");
        }

        return appointments.stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Zwraca historię wizyt pacjenta filtrowaną po statusie (np. "COMPLETED").
     */
    @Transactional(readOnly = true)
    public List<PatientVisitHistoryDTO> getPatientHistoryByStatus(Long tenantId, Long patientId, String status) {
        return getPatientHistory(tenantId, patientId)
                .stream()
                .filter(v -> v.status().equalsIgnoreCase(status))
                .toList();
    }

    private PatientVisitHistoryDTO toDTO(Appointment a) {
        return new PatientVisitHistoryDTO(
                a.getId(),
                a.getTenantId(),
                a.getLocationId(),
                a.getRoomId(),
                a.getDentistStaffId(),
                a.getServiceItemId(),
                a.getStartAt(),
                a.getEndAt(),
                a.getStatus(),
                a.getNotes()
        );
    }
}
