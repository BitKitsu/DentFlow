package pl.edu.ur.dentflow.core.patient.application;

import pl.edu.ur.dentflow.core.patient.api.CreatePatientRequest;
import pl.edu.ur.dentflow.core.patient.api.PatientResponse;
import pl.edu.ur.dentflow.core.patient.api.UpdatePatientRequest;
import pl.edu.ur.dentflow.core.patient.domain.Patient;
import pl.edu.ur.dentflow.core.patient.infrastructure.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final JdbcTemplate jdbcTemplate;

    public PatientService(PatientRepository patientRepository, JdbcTemplate jdbcTemplate) {
        this.patientRepository = patientRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    private Map<Long, String> fetchUserAvatars(Set<Long> userIds) {
        if (userIds.isEmpty()) return Collections.emptyMap();
        String placeholders = userIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, avatar_url FROM \"user\" WHERE id IN (" + placeholders + ")"
        );
        return rows.stream().collect(Collectors.toMap(
                row -> ((Number) row.get("id")).longValue(),
                row -> row.get("avatar_url") != null ? row.get("avatar_url").toString() : null
        ));
    }

    private PatientResponse enrichWithUserAvatar(PatientResponse response, Map<Long, String> userAvatars) {
        if (response.avatarUrl() != null || response.userId() == null) return response;
        String userAvatar = userAvatars.get(response.userId());
        if (userAvatar == null) return response;
        return new PatientResponse(
                response.id(), response.tenantId(), response.userId(),
                response.firstName(), response.lastName(), response.phone(),
                response.email(), response.notes(), response.dateOfBirth(),
                response.pesel(), response.gender(),
                response.addressStreet(), response.addressCity(),
                response.addressZip(), response.addressCountry(),
                userAvatar
        );
    }

    private void requireTenantExists(Long tenantId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM tenant WHERE id = ?)", Boolean.class, tenantId);
        if (!Boolean.TRUE.equals(exists)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Klinika nie istnieje");
        }
    }

    public List<PatientResponse> getPatients(Long tenantId, String searchTerm) {
        requireTenantExists(tenantId);
        List<Patient> patients;
        if (searchTerm != null && !searchTerm.isBlank()) {
            patients = patientRepository.searchPatients(tenantId, searchTerm);
        } else {
            patients = patientRepository.findByTenantId(tenantId);
        }
        Set<Long> userIds = patients.stream()
                .map(Patient::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> userAvatars = fetchUserAvatars(userIds);
        return patients.stream()
                .map(PatientResponse::from)
                .map(r -> enrichWithUserAvatar(r, userAvatars))
                .toList();
    }

    public PatientResponse getPatient(Long tenantId, Long patientId) {
        Patient patient = patientRepository.findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pacjent nie istnieje"));
        PatientResponse response = PatientResponse.from(patient);
        if (response.avatarUrl() == null && response.userId() != null) {
            Map<Long, String> userAvatars = fetchUserAvatars(Set.of(response.userId()));
            response = enrichWithUserAvatar(response, userAvatars);
        }
        return response;
    }

    @Transactional
    public PatientResponse addPatient(Long tenantId, CreatePatientRequest request) {
        requireTenantExists(tenantId);
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

        PatientResponse response = PatientResponse.from(patientRepository.save(patient));
        if (response.avatarUrl() == null && response.userId() != null) {
            Map<Long, String> userAvatars = fetchUserAvatars(Set.of(response.userId()));
            response = enrichWithUserAvatar(response, userAvatars);
        }
        return response;
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

        PatientResponse response = PatientResponse.from(patientRepository.save(patient));
        if (response.avatarUrl() == null && response.userId() != null) {
            Map<Long, String> userAvatars = fetchUserAvatars(Set.of(response.userId()));
            response = enrichWithUserAvatar(response, userAvatars);
        }
        return response;
    }

    @Transactional
    public void deletePatient(Long tenantId, Long patientId) {
        Patient patient = patientRepository.findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pacjent nie istnieje"));
        patientRepository.delete(patient);
    }

    @Transactional
    public PatientResponse ensurePatientForUser(Long tenantId, Long userId, String firstName, String lastName, String email, String phone) {
        PatientResponse response = patientRepository.findByTenantIdAndUserId(tenantId, userId)
                .map(existing -> {
                    if (firstName != null && !firstName.isEmpty()) existing.setFirstName(firstName);
                    if (lastName != null && !lastName.isEmpty()) existing.setLastName(lastName);
                    if (email != null && !email.isEmpty()) existing.setEmail(email);
                    if (phone != null && !phone.isEmpty()) existing.setPhone(phone);
                    return PatientResponse.from(patientRepository.save(existing));
                })
                .orElseGet(() -> {
                    Patient patient = Patient.builder()
                            .tenantId(tenantId)
                            .userId(userId)
                            .firstName(firstName)
                            .lastName(lastName)
                            .email(email)
                            .phone(phone)
                            .build();
                    return PatientResponse.from(patientRepository.save(patient));
                });
        if (response.avatarUrl() == null && response.userId() != null) {
            Map<Long, String> userAvatars = fetchUserAvatars(Set.of(response.userId()));
            response = enrichWithUserAvatar(response, userAvatars);
        }
        return response;
    }
}
