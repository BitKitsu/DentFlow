package com.dentflow.core.audit.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "entity_name", nullable = false, length = 50)
    private String entityName;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(name = "performed_by")
    private Long performedBy;

    @Column(nullable = false, columnDefinition = "TIMESTAMPTZ")
    @Builder.Default
    private OffsetDateTime timestamp = OffsetDateTime.now();

    @Column(columnDefinition = "JSONB")
    private String details;
}
