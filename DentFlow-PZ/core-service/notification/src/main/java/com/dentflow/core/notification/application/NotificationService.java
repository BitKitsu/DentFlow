package com.dentflow.core.notification.application;

import com.dentflow.core.notification.api.CreateNotificationRequest;
import com.dentflow.core.notification.api.NotificationResponse;
import com.dentflow.core.notification.domain.Notification;
import com.dentflow.core.notification.infrastructure.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationResponse> getUserNotifications(Long tenantId, Long userId, boolean unreadOnly) {
        List<Notification> notifications;
        if (unreadOnly) {
            notifications = notificationRepository.findByTenantIdAndUserIdAndReadFalseOrderByCreatedAtDesc(tenantId, userId);
        } else {
            notifications = notificationRepository.findByTenantIdAndUserIdOrderByCreatedAtDesc(tenantId, userId);
        }
        return notifications.stream().map(NotificationResponse::from).toList();
    }

    public long getUnreadCount(Long tenantId, Long userId) {
        return notificationRepository.countByTenantIdAndUserIdAndReadFalse(tenantId, userId);
    }

    @Transactional
    public NotificationResponse createNotification(Long tenantId, CreateNotificationRequest request) {
        Notification notification = Notification.builder()
                .tenantId(tenantId)
                .userId(request.userId())
                .type(request.type())
                .message(request.message())
                .build();
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Transactional
    public NotificationResponse markAsRead(Long tenantId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndTenantId(notificationId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Powiadomienie nie istnieje"));
        
        notification.setRead(true);
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(Long tenantId, Long userId) {
        List<Notification> unread = notificationRepository.findByTenantIdAndUserIdAndReadFalseOrderByCreatedAtDesc(tenantId, userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
