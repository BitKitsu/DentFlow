package com.dentflow.core.patient.application;

import com.dentflow.core.patient.api.CreatePatientRequest;
import com.dentflow.core.patient.api.PatientResponse;
import com.dentflow.core.patient.domain.Patient;
import com.dentflow.core.patient.infrastructure.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .id(1L)
                .tenantId(100L)
                .firstName("John")
                .lastName("Doe")
                .phone("123456789")
                .build();
    }

    @Test
    void shouldReturnPatientsWhenSearching() {
        when(patientRepository.searchPatients(100L, "John")).thenReturn(List.of(patient));

        List<PatientResponse> responses = patientService.getPatients(100L, "John");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).firstName()).isEqualTo("John");
    }

    @Test
    void shouldReturnAllPatientsWhenNotSearching() {
        when(patientRepository.findByTenantId(100L)).thenReturn(List.of(patient));

        List<PatientResponse> responses = patientService.getPatients(100L, null);

        assertThat(responses).hasSize(1);
    }

    @Test
    void shouldAddPatient() {
        CreatePatientRequest request = new CreatePatientRequest(null, "Jane", "Doe", null, null, null);
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient p = invocation.getArgument(0);
            p.setId(2L);
            return p;
        });

        PatientResponse response = patientService.addPatient(100L, request);

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.firstName()).isEqualTo("Jane");
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void shouldDeletePatient() {
        when(patientRepository.findByIdAndTenantId(1L, 100L)).thenReturn(Optional.of(patient));

        patientService.deletePatient(100L, 1L);

        verify(patientRepository).delete(patient);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistent() {
        when(patientRepository.findByIdAndTenantId(99L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.deletePatient(100L, 99L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldThrowNotFoundWhenGettingNonExistent() {
        when(patientRepository.findByIdAndTenantId(99L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatient(100L, 99L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistent() {
        when(patientRepository.findByIdAndTenantId(99L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.updatePatient(100L, 99L, null))
                .isInstanceOf(ResponseStatusException.class);
    }
}
