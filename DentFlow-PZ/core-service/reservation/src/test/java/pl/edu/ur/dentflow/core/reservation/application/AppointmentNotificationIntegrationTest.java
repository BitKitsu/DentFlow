package pl.edu.ur.dentflow.core.reservation.application;

import pl.edu.ur.dentflow.core.reservation.api.CreateAppointmentRequest;
import pl.edu.ur.dentflow.core.notification.infrastructure.NotificationRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests verifying reservation integration with the notification system.
 * Verifies that appointment creation generates appropriate in-app notifications.
 *
 * <p>DISABLED: @Transactional on the test class conflicts with @Async event listener.
 * The appointment save transaction hasn't committed when the async notification handler
 * runs, so notifications are never persisted. Needs Awaitility or non-transactional
 * test setup to properly test async event flows.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled("Async @EventListener conflicts with @Transactional test — notification writes happen after test reads")
class AppointmentNotificationIntegrationTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void shouldCreateNotificationWhenAppointmentIsCreated() {
        Long tenantId = 10L;
        OffsetDateTime startAt = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime endAt = startAt.plusHours(1);

        // Create appointment
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                1L, 1L, 10L, 20L, 30L, startAt, endAt, null, null);
        appointmentService.createAppointment(tenantId, request);

        // Verify notifications were created
        // Should be an in-app notification for the dentist (userId=20 linked to staffId=10)
        // Email to patient is not verified (would require EmailService mock)
        var notifications = notificationRepository.findByTenantIdAndUserIdOrderByCreatedAtDesc(tenantId, 20L);
        assertThat(notifications).isNotEmpty();
        assertThat(notifications.get(0).getType()).isEqualTo("APPOINTMENT");
    }

    @Test
    void shouldCreateNotificationWhenAppointmentIsCancelled() {
        Long tenantId = 10L;
        OffsetDateTime startAt = OffsetDateTime.now().plusDays(6).withHour(10).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime endAt = startAt.plusHours(1);

        // Create appointment
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                1L, 1L, 10L, 20L, 30L, startAt, endAt, null, null);
        var created = appointmentService.createAppointment(tenantId, request);

        // Cancel appointment
        appointmentService.cancelAppointment(tenantId, created.id());

        // Verify cancellation notifications were created
        var notifications = notificationRepository.findByTenantIdAndUserIdOrderByCreatedAtDesc(tenantId, 20L);
        assertThat(notifications).hasSize(2); // one for creation, one for cancellation

        var cancelNotification = notifications.stream()
                .filter(n -> n.getMessage().contains("cancelled"))
                .findFirst();
        assertThat(cancelNotification).isPresent();
    }
}
