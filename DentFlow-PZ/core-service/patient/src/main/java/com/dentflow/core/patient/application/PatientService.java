package com.dentflow.core.patient.application;

import com.dentflow.core.patient.api.CreatePatientRequest;
import com.dentflow.core.patient.api.PatientResponse;
import com.dentflow.core.patient.api.UpdatePatientRequest;
import com.dentflow.core.patient.domain.Patient;
import com.dentflow.core.patient.infrastructure.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientResponse> getPatients(Long tenantId, String searchTerm) {
        List<Patient> patients;
        if (searchTerm != null && !searchTerm.isBlank()) {
            patients = patientRepository.searchPatients(tenantId, searchTerm);
        } else {
            patients = patientRepository.findByTenantId(tenantId);
        }
        return patients.stream().map(PatientResponse::from).toList();
    }

    public PatientResponse getPatient(Long tenantId, Long patientId) {
        Patient patient = patientRepository.findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pacjent nie istnieje"));
        return PatientResponse.from(patient);
    }

    @Transactional
    public PatientResponse addPatient(Long tenantId, CreatePatientRequest request) {
        Patient patient = Patient.builder()
                .tenantId(tenantId)
                .userId(request.userId())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .email(request.email())
                .notes(request.notes())
                .build();

        return PatientResponse.from(patientRepository.save(patient));
    }

    @Transactional
    public PatientResponse updatePatient(Long tenantId, Long patientId, UpdatePatientRequest request) {
        Patient patient = patientRepository.findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pacjent nie istnieje"));

        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setNotes(request.notes());
        if (request.userId() != null) {
            patient.setUserId(request.userId());
        }

        return PatientResponse.from(patientRepository.save(patient));
    }

    @Transactional
    public void deletePatient(Long tenantId, Long patientId) {
        Patient patient = patientRepository.findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pacjent nie istnieje"));
        patientRepository.delete(patient);
    }
}
