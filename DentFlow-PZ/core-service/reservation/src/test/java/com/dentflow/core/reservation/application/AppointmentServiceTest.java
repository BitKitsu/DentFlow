package com.dentflow.core.reservation.application;

import com.dentflow.core.reservation.api.AppointmentResponse;
import com.dentflow.core.reservation.api.CreateAppointmentRequest;
import com.dentflow.core.reservation.api.UpdateAppointmentRequest;
import com.dentflow.core.reservation.domain.Appointment;
import com.dentflow.core.reservation.infrastructure.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private final OffsetDateTime now = OffsetDateTime.now();
    private final OffsetDateTime later = now.plusHours(1);

    private Appointment appointment;

    @BeforeEach
    void setUp() {
        appointment = Appointment.builder()
                .id(1L).tenantId(10L)
                .locationId(5L).dentistStaffId(20L).patientId(30L)
                .startAt(now).endAt(later).status("SCHEDULED")
                .build();
    }

    @Test
    void shouldReturnAllAppointments() {
        when(appointmentRepository.findByTenantId(10L)).thenReturn(List.of(appointment));

        List<AppointmentResponse> result = appointmentService.getAppointments(10L, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo("SCHEDULED");
    }

    @Test
    void shouldReturnAppointmentsByDateRange() {
        when(appointmentRepository.findByTenantIdAndDateRange(10L, now, later))
                .thenReturn(List.of(appointment));

        List<AppointmentResponse> result = appointmentService.getAppointments(10L, now, later);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldCreateAppointmentSuccessfully() {
        CreateAppointmentRequest req = new CreateAppointmentRequest(
                5L, null, 20L, 30L, null, now, later, null, null);
        when(appointmentRepository.findConflicting(10L, 20L, now, later)).thenReturn(List.of());
        when(appointmentRepository.save(any())).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(99L);
            return a;
        });

        AppointmentResponse response = appointmentService.createAppointment(10L, req);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.status()).isEqualTo("SCHEDULED");
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void shouldThrowConflictWhenDentistBusy() {
        CreateAppointmentRequest req = new CreateAppointmentRequest(
                5L, null, 20L, 30L, null, now, later, null, null);
        when(appointmentRepository.findConflicting(10L, 20L, now, later))
                .thenReturn(List.of(appointment));

        assertThatThrownBy(() -> appointmentService.createAppointment(10L, req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("CONFLICT");
    }

    @Test
    void shouldThrowBadRequestWhenEndBeforeStart() {
        CreateAppointmentRequest req = new CreateAppointmentRequest(
                5L, null, 20L, 30L, null, later, now, null, null);

        assertThatThrownBy(() -> appointmentService.createAppointment(10L, req))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldCancelAppointment() {
        when(appointmentRepository.findByIdAndTenantId(1L, 10L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);

        AppointmentResponse response = appointmentService.cancelAppointment(10L, 1L);

        assertThat(response.status()).isEqualTo("CANCELLED");
    }

    @Test
    void shouldThrowWhenCancellingAlreadyCancelled() {
        appointment.setStatus("CANCELLED");
        when(appointmentRepository.findByIdAndTenantId(1L, 10L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(10L, 1L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldCompleteAppointment() {
        when(appointmentRepository.findByIdAndTenantId(1L, 10L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);

        AppointmentResponse response = appointmentService.completeAppointment(10L, 1L);

        assertThat(response.status()).isEqualTo("COMPLETED");
    }

    @Test
    void shouldThrowNotFoundWhenAppointmentMissing() {
        when(appointmentRepository.findByIdAndTenantId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointment(10L, 99L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldUpdateAppointment() {
        OffsetDateTime newStart = now.plusDays(1);
        OffsetDateTime newEnd = newStart.plusHours(1);
        UpdateAppointmentRequest req = new UpdateAppointmentRequest(newStart, newEnd, null, null, "Nowa notatka");

        when(appointmentRepository.findByIdAndTenantId(1L, 10L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.findConflicting(10L, 20L, newStart, newEnd)).thenReturn(List.of());
        when(appointmentRepository.save(any())).thenReturn(appointment);

        AppointmentResponse response = appointmentService.updateAppointment(10L, 1L, req);

        assertThat(response).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }
}
