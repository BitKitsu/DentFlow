package com.dentflow.core.notification.application;

import com.dentflow.core.notification.api.CreateNotificationRequest;
import com.dentflow.core.notification.api.NotificationResponse;
import com.dentflow.core.notification.domain.Notification;
import com.dentflow.core.notification.infrastructure.NotificationRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .id(1L)
                .tenantId(10L)
                .userId(100L)
                .type("SYSTEM")
                .message("Test message")
                .read(false)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void shouldGetUserNotifications() {
        when(notificationRepository.findByTenantIdAndUserIdOrderByCreatedAtDesc(10L, 100L))
                .thenReturn(List.of(notification));

        List<NotificationResponse> result = notificationService.getUserNotifications(10L, 100L, false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).message()).isEqualTo("Test message");
    }

    @Test
    void shouldGetUnreadNotifications() {
        when(notificationRepository.findByTenantIdAndUserIdAndReadFalseOrderByCreatedAtDesc(10L, 100L))
                .thenReturn(List.of(notification));

        List<NotificationResponse> result = notificationService.getUserNotifications(10L, 100L, true);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetUnreadCount() {
        when(notificationRepository.countByTenantIdAndUserIdAndReadFalse(10L, 100L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(10L, 100L);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    void shouldCreateNotification() {
        CreateNotificationRequest request = new CreateNotificationRequest(100L, "SYSTEM", "Hello");
        when(notificationRepository.save(any())).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(99L);
            return n;
        });

        NotificationResponse response = notificationService.createNotification(10L, request);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.message()).isEqualTo("Hello");
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void shouldMarkAsRead() {
        when(notificationRepository.findByIdAndTenantId(1L, 10L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenReturn(notification);

        NotificationResponse response = notificationService.markAsRead(10L, 1L);

        assertThat(response.read()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void shouldThrowNotFoundWhenMarkingUnknownNotificationAsRead() {
        when(notificationRepository.findByIdAndTenantId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(10L, 99L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldMarkAllAsRead() {
        when(notificationRepository.findByTenantIdAndUserIdAndReadFalseOrderByCreatedAtDesc(10L, 100L))
                .thenReturn(List.of(notification));

        notificationService.markAllAsRead(10L, 100L);

        assertThat(notification.getRead()).isTrue();
        verify(notificationRepository).saveAll(anyList());
    }
}
