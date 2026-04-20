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
}
