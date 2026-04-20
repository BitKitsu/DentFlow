package com.dentflow.core.scheduling.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "blocker")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Blocker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "start_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime endAt;

    @Column(length = 255)
    private String reason;
}
