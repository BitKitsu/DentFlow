package com.dentflow.core.notification.api;

import com.dentflow.core.notification.application.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenants/{tenantId}/users/{userId}/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @PathVariable Long tenantId,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return ResponseEntity.ok(notificationService.getUserNotifications(tenantId, userId, unreadOnly));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable Long tenantId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(tenantId, userId));
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @PathVariable Long tenantId,
            @PathVariable Long userId,
            @Valid @RequestBody CreateNotificationRequest request) {
        // userId in path vs body check could be added, here we trust request object.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.createNotification(tenantId, request));
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long tenantId,
            @PathVariable Long userId,
            @PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(tenantId, notificationId));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @PathVariable Long tenantId,
            @PathVariable Long userId) {
        notificationService.markAllAsRead(tenantId, userId);
        return ResponseEntity.noContent().build();
    }
}
