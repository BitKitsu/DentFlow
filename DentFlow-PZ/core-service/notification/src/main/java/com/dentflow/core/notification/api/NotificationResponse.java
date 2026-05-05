package com.dentflow.core.notification.api;

import com.dentflow.core.notification.domain.Notification;
import java.time.OffsetDateTime;

public record NotificationResponse(
        Long id,
        Long tenantId,
        Long userId,
        String type,
        String message,
        boolean read,
        OffsetDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTenantId(),
                n.getUserId(),
                n.getType(),
                n.getMessage(),
                n.getRead(),
                n.getCreatedAt()
        );
    }
}
