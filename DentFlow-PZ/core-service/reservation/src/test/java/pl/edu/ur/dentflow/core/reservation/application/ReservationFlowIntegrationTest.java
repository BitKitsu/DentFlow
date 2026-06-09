package pl.edu.ur.dentflow.core.reservation.application;

import pl.edu.ur.dentflow.core.reservation.api.AppointmentResponse;
import pl.edu.ur.dentflow.core.reservation.api.CreateAppointmentRequest;
import pl.edu.ur.dentflow.core.reservation.api.UpdateAppointmentRequest;
import pl.edu.ur.dentflow.core.reservation.infrastructure.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.springframework.web.server.ResponseStatusException;

/**
 * Integration tests for the reservation flow (end-to-end).
 * Verifies the complete cycle: create → update → cancel → complete appointment.
 *
 * <p>Tests run the full Spring Boot context with H2 in-memory database.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:schema.sql"
})
@Transactional
class ReservationFlowIntegrationTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    void shouldCompleteFullReservationFlow() {
        Long tenantId = 10L;
        OffsetDateTime startAt = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime endAt = startAt.plusHours(1);

        // 1. Create appointment
        CreateAppointmentRequest createRequest = new CreateAppointmentRequest(
                1L, 1L, 10L, 20L, 30L, startAt, endAt, null, "Test appointment");
        AppointmentResponse created = appointmentService.createAppointment(tenantId, createRequest);

        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();
        assertThat(created.status()).isEqualTo("SCHEDULED");
        assertThat(created.startAt()).isEqualTo(startAt);
        assertThat(created.endAt()).isEqualTo(endAt);

        // 2. Fetch appointment
        AppointmentResponse fetched = appointmentService.getAppointment(tenantId, created.id());
        assertThat(fetched.id()).isEqualTo(created.id());
        assertThat(fetched.notes()).isEqualTo("Test appointment");

        // 3. Update appointment
        OffsetDateTime newStart = startAt.plusDays(2);
        OffsetDateTime newEnd = newStart.plusHours(2);
        UpdateAppointmentRequest updateRequest = new UpdateAppointmentRequest(
                newStart, newEnd, 30L, 31L, "Updated appointment");
        AppointmentResponse updated = appointmentService.updateAppointment(tenantId, created.id(), updateRequest);

        assertThat(updated.startAt()).isEqualTo(newStart);
        assertThat(updated.endAt()).isEqualTo(newEnd);
        assertThat(updated.notes()).isEqualTo("Updated appointment");

        // 4. Complete appointment
        AppointmentResponse completed = appointmentService.completeAppointment(tenantId, created.id());
        assertThat(completed.status()).isEqualTo("COMPLETED");
    }

    @Test
    void shouldCancelReservation() {
        Long tenantId = 10L;
        OffsetDateTime startAt = OffsetDateTime.now().plusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime endAt = startAt.plusHours(1);

        // 1. Create appointment
        CreateAppointmentRequest createRequest = new CreateAppointmentRequest(
                1L, 1L, 10L, 20L, 30L, startAt, endAt, null, null);
        AppointmentResponse created = appointmentService.createAppointment(tenantId, createRequest);

        // 2. Cancel appointment
        AppointmentResponse cancelled = appointmentService.cancelAppointment(tenantId, created.id());
        assertThat(cancelled.status()).isEqualTo("CANCELLED");

        // 3. Attempt to cancel again (should fail)
        assertThatThrownBy(() -> appointmentService.cancelAppointment(tenantId, created.id()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldDetectDentistConflict() {
        Long tenantId = 10L;
        OffsetDateTime startAt = OffsetDateTime.now().plusDays(4).withHour(9).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime endAt = startAt.plusHours(2);

        // 1. Create first appointment
        CreateAppointmentRequest request1 = new CreateAppointmentRequest(
                1L, 1L, 10L, 20L, 30L, startAt, endAt, null, null);
        appointmentService.createAppointment(tenantId, request1);

        // 2. Attempt to create appointment at same time for same dentist
        CreateAppointmentRequest request2 = new CreateAppointmentRequest(
                1L, 1L, 10L, 20L, 31L,
                startAt.plusMinutes(30), endAt.plusMinutes(30), null, null);
        assertThatThrownBy(() -> appointmentService.createAppointment(tenantId, request2))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(409);
                });
    }

    @Test
    void shouldRejectAppointmentWithInvalidTimeRange() {
        Long tenantId = 10L;
        OffsetDateTime startAt = OffsetDateTime.now().plusDays(5).withHour(10).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime endAt = startAt.minusHours(1); // endAt before startAt

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                1L, 1L, 10L, 20L, 30L, startAt, endAt, null, null);
        assertThatThrownBy(() -> appointmentService.createAppointment(tenantId, request))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldGetAppointmentsByDateRange() {
        Long tenantId = 10L;
        OffsetDateTime baseDate = OffsetDateTime.now().plusDays(10).withHour(0).withMinute(0).withSecond(0).withNano(0);

        // Create two appointments at different times
        CreateAppointmentRequest request1 = new CreateAppointmentRequest(
                1L, 1L, 10L, 20L, 30L,
                baseDate.withHour(9), baseDate.withHour(10), null, null);
        appointmentService.createAppointment(tenantId, request1);

        CreateAppointmentRequest request2 = new CreateAppointmentRequest(
                1L, 1L, 10L, 20L, 31L,
                baseDate.withHour(14), baseDate.withHour(15), null, null);
        appointmentService.createAppointment(tenantId, request2);

        // Fetch appointments in 8:00 - 12:00 range
        var morningAppointments = appointmentService.getAppointments(
                tenantId,
                baseDate.withHour(8),
                baseDate.withHour(12));
        assertThat(morningAppointments).hasSize(1);

        // Fetch appointments in 12:00 - 18:00 range
        var afternoonAppointments = appointmentService.getAppointments(
                tenantId,
                baseDate.withHour(12),
                baseDate.withHour(18));
        assertThat(afternoonAppointments).hasSize(1);

        // Fetch all appointments
        var allAppointments = appointmentService.getAppointments(tenantId, null, null);
        assertThat(allAppointments).hasSize(2);
    }

    @Test
    void shouldRejectNonexistentAppointment() {
        Long tenantId = 10L;
        assertThatThrownBy(() -> appointmentService.getAppointment(tenantId, 999L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(404);
                });
    }
}
