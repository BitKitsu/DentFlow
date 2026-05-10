package com.dentflow.core.notification.infrastructure;

import com.dentflow.core.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByTenantIdAndUserIdOrderByCreatedAtDesc(Long tenantId, Long userId);

    List<Notification> findByTenantIdAndUserIdAndReadFalseOrderByCreatedAtDesc(Long tenantId, Long userId);

    long countByTenantIdAndUserIdAndReadFalse(Long tenantId, Long userId);

    Optional<Notification> findByIdAndTenantId(Long id, Long tenantId);
}
