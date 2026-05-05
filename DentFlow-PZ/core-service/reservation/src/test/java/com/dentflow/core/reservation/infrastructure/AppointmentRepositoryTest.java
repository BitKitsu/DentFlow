package com.dentflow.core.reservation.infrastructure;

import com.dentflow.core.reservation.domain.Appointment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldSearchAppointmentsDetailsWithFilters() {
        // given
        // Wstawiamy mockowe dane do tabel z innych modułów za pomocą jdbcTemplate
        jdbcTemplate.execute("INSERT INTO patient (id, first_name, last_name) VALUES (1, 'Jan', 'Kowalski')");
        jdbcTemplate.execute("INSERT INTO staff_member (id, display_name) VALUES (2, 'Dr. Ząb')");
        jdbcTemplate.execute("INSERT INTO location (id, name) VALUES (3, 'Klinika Główna')");
        jdbcTemplate.execute("INSERT INTO room (id, name) VALUES (4, 'Gabinet 1')");

        Appointment appointment = Appointment.builder()
                .tenantId(10L)
                .patientId(1L)
                .dentistStaffId(2L)
                .locationId(3L)
                .roomId(4L)
                .startAt(OffsetDateTime.parse("2026-05-10T10:00:00Z"))
                .endAt(OffsetDateTime.parse("2026-05-10T11:00:00Z"))
                .status("SCHEDULED")
                .notes("Ból zęba")
                .build();
        appointmentRepository.save(appointment);

        // when
        List<AppointmentDetailsProjection> results = appointmentRepository.searchAppointmentsDetails(
                10L, null, null, null);

        // then
        assertThat(results).hasSize(1);
        AppointmentDetailsProjection proj = results.get(0);
        assertThat(proj.getPatientFirstName()).isEqualTo("Jan");
        assertThat(proj.getPatientLastName()).isEqualTo("Kowalski");
        assertThat(proj.getDentistName()).isEqualTo("Dr. Ząb");
        assertThat(proj.getLocationName()).isEqualTo("Klinika Główna");
        assertThat(proj.getRoomName()).isEqualTo("Gabinet 1");
        assertThat(proj.getNotes()).isEqualTo("Ból zęba");
        assertThat(proj.getStatus()).isEqualTo("SCHEDULED");
    }

    @Test
    void shouldFindConflictingAppointments() {
        // given
        Appointment existing = Appointment.builder()
                .tenantId(10L)
                .patientId(1L)
                .dentistStaffId(2L)
                .locationId(3L)
                .startAt(OffsetDateTime.parse("2026-05-10T10:00:00Z"))
                .endAt(OffsetDateTime.parse("2026-05-10T11:00:00Z"))
                .status("SCHEDULED")
                .build();
        appointmentRepository.save(existing);

        // when
        List<Appointment> conflicts = appointmentRepository.findConflicting(
                10L,
                2L,
                OffsetDateTime.parse("2026-05-10T10:30:00Z"),
                OffsetDateTime.parse("2026-05-10T11:30:00Z")
        );

        // then
        assertThat(conflicts).hasSize(1);
        assertThat(conflicts.get(0).getId()).isEqualTo(existing.getId());
    }
}
