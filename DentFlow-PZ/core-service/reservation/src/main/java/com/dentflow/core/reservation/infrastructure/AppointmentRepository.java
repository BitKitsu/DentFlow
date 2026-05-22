package com.dentflow.core.reservation.infrastructure;

import com.dentflow.core.reservation.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

       List<Appointment> findByTenantId(Long tenantId);

       Optional<Appointment> findByIdAndTenantId(Long id, Long tenantId);

       List<Appointment> findByPatientId(Long patientId);

       List<Appointment> findByDentistStaffId(Long dentistStaffId);

       @Query("SELECT a FROM Appointment a WHERE a.tenantId = :tenantId " +
                     "AND a.startAt >= :from AND a.startAt < :to " +
                     "ORDER BY a.startAt")
       List<Appointment> findByTenantIdAndDateRange(
                     @Param("tenantId") Long tenantId,
                     @Param("from") OffsetDateTime from,
                     @Param("to") OffsetDateTime to);

       @Query("SELECT a FROM Appointment a WHERE a.tenantId = :tenantId " +
                     "AND a.dentistStaffId = :dentistStaffId " +
                     "AND a.status NOT IN ('CANCELLED') " +
                     "AND a.startAt < :end AND a.endAt > :start")
       List<Appointment> findConflicting(
                     @Param("tenantId") Long tenantId,
                     @Param("dentistStaffId") Long dentistStaffId,
                     @Param("start") OffsetDateTime start,
                     @Param("end") OffsetDateTime end);

       @Query("SELECT a FROM Appointment a WHERE a.tenantId = :tenantId " +
                     "AND a.patientId = :patientId " +
                     "ORDER BY a.startAt DESC")
       List<Appointment> findByTenantIdAndPatientIdOrderByStartAtDesc(
                     @Param("tenantId") Long tenantId,
                     @Param("patientId") Long patientId);

       List<Appointment> findByTenantIdAndStatus(Long tenantId, String status);

       @Query("SELECT a FROM Appointment a WHERE a.tenantId = :tenantId " +
                     "AND a.roomId = :roomId " +
                     "AND a.status NOT IN ('CANCELLED') " +
                     "AND a.startAt >= :from AND a.startAt < :to")
       List<Appointment> findByTenantIdAndRoomIdAndDateRange(
                     @Param("tenantId") Long tenantId,
                     @Param("roomId") Long roomId,
                     @Param("from") OffsetDateTime from,
                     @Param("to") OffsetDateTime to);

       @Query("SELECT DISTINCT a.roomId FROM Appointment a WHERE a.tenantId = :tenantId " +
                     "AND a.status NOT IN ('CANCELLED') " +
                     "AND a.startAt >= :from AND a.startAt < :to")
       List<Long> findDistinctRoomIdsByTenantIdAndDateRange(
                     @Param("tenantId") Long tenantId,
                     @Param("from") OffsetDateTime from,
                     @Param("to") OffsetDateTime to);

    @Query(value = """
            SELECT 
                a.id as appointmentId,
                a.start_at as startAt,
                a.end_at as endAt,
                a.status as status,
                p.first_name as patientFirstName,
                p.last_name as patientLastName,
                s.display_name as dentistName,
                l.name as locationName,
                r.name as roomName,
                a.notes as notes
            FROM appointment a
            JOIN patient p ON a.patient_id = p.id
            JOIN staff_member s ON a.dentist_staff_id = s.id
            JOIN location l ON a.location_id = l.id
            LEFT JOIN room r ON a.room_id = r.id
            WHERE a.tenant_id = :tenantId
            AND (:status IS NULL OR a.status = :status)
            AND (:dentistId IS NULL OR a.dentist_staff_id = :dentistId)
            AND (:patientId IS NULL OR a.patient_id = :patientId)
            ORDER BY a.start_at DESC
            """, nativeQuery = true)
    List<AppointmentDetailsProjection> searchAppointmentsDetails(
            @Param("tenantId") Long tenantId,
            @Param("status") String status,
            @Param("dentistId") Long dentistId,
            @Param("patientId") Long patientId
    );
}
