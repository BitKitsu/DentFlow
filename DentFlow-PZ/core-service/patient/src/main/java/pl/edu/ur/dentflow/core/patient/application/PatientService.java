package pl.edu.ur.dentflow.core.patient.application;

import pl.edu.ur.dentflow.core.patient.api.CreatePatientRequest;
import pl.edu.ur.dentflow.core.patient.api.PatientResponse;
import pl.edu.ur.dentflow.core.patient.api.UpdatePatientRequest;
import pl.edu.ur.dentflow.core.patient.domain.Patient;
import pl.edu.ur.dentflow.core.patient.infrastructure.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service managing patient data in the DentFlow system.
 * Handles CRUD operations and patient search within a clinic (tenant).
 *
 * <p>Patients are associated with user accounts (optionally) and assigned
 * to a specific clinic (tenant) via tenantId.</p>
 *
 * <p>Search is performed by first name, last name, email or phone number.</p>
 *
 * @see pl.edu.ur.dentflow.core.patient.domain.Patient
 * @see pl.edu.ur.dentflow.core.patient.infrastructure.PatientRepository
 */
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
                .dateOfBirth(request.dateOfBirth())
                .pesel(request.pesel())
                .gender(request.gender())
                .addressStreet(request.addressStreet())
                .addressCity(request.addressCity())
                .addressZip(request.addressZip())
                .addressCountry(request.addressCountry())
                .avatarUrl(request.avatarUrl())
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
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setPesel(request.pesel());
        patient.setGender(request.gender());
        patient.setAddressStreet(request.addressStreet());
        patient.setAddressCity(request.addressCity());
        patient.setAddressZip(request.addressZip());
        patient.setAddressCountry(request.addressCountry());
        if (request.userId() != null) {
            patient.setUserId(request.userId());
        }
        if (request.avatarUrl() != null) {
            patient.setAvatarUrl(request.avatarUrl());
        }

        return PatientResponse.from(patientRepository.save(patient));
    }

    @Transactional
    public void deletePatient(Long tenantId, Long patientId) {
        Patient patient = patientRepository.findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pacjent nie istnieje"));
        patientRepository.delete(patient);
    }

    @Transactional
    public PatientResponse ensurePatientForUser(Long tenantId, Long userId, String firstName, String lastName, String email) {
        return patientRepository.findByTenantIdAndUserId(tenantId, userId)
                .map(PatientResponse::from)
                .orElseGet(() -> {
                    Patient patient = Patient.builder()
                            .tenantId(tenantId)
                            .userId(userId)
                            .firstName(firstName)
                            .lastName(lastName)
                            .email(email)
                            .build();
                    return PatientResponse.from(patientRepository.save(patient));
                });
    }
}
