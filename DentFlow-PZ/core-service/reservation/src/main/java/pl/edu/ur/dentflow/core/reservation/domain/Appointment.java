package pl.edu.ur.dentflow.core.reservation.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Entity representing an appointment (reservation) in the DentFlow system.
 *
 * <p>An appointment is associated with:
 * <ul>
 *   <li>Clinic (tenantId)</li>
 *   <li>Location (locationId) and treatment room (roomId)</li>
 *   <li>Dentist (dentistStaffId) and patient (patientId)</li>
 *   <li>Optionally a service from the catalog (serviceItemId)</li>
 * </ul>
 *
 * <p>Appointment states:
 * <ul>
 *   <li>SCHEDULED - scheduled (default state after creation)</li>
 *   <li>COMPLETED - completed</li>
 *   <li>CANCELLED - canceled</li>
 *   <li>NO_SHOW - patient did not show up</li>
 * </ul>
 *
 * @see pl.edu.ur.dentflow.core.reservation.application.AppointmentService
 */
@Entity
@Table(name = "appointment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "dentist_staff_id", nullable = false)
    private Long dentistStaffId;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "service_item_id")
    private Long serviceItemId;

    @Column(name = "start_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime endAt;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "SCHEDULED";

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
