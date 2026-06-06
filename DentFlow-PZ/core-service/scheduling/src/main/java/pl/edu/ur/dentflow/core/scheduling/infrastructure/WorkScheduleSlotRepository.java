package pl.edu.ur.dentflow.core.scheduling.infrastructure;

import pl.edu.ur.dentflow.core.scheduling.domain.WorkScheduleSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface WorkScheduleSlotRepository extends JpaRepository<WorkScheduleSlot, Long> {

    List<WorkScheduleSlot> findByTenantId(Long tenantId);

    List<WorkScheduleSlot> findByStaffId(Long staffId);

    @Query("SELECT w FROM WorkScheduleSlot w WHERE w.tenantId = :tenantId " +
           "AND w.staffId = :staffId " +
           "AND w.startAt < :end AND w.endAt > :start")
    List<WorkScheduleSlot> findOverlapping(
            @Param("tenantId") Long tenantId,
            @Param("staffId") Long staffId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end);

    @Query("SELECT w FROM WorkScheduleSlot w WHERE w.tenantId = :tenantId " +
           "AND w.startAt >= :from AND w.startAt < :to")
    List<WorkScheduleSlot> findByTenantIdAndDateRange(
            @Param("tenantId") Long tenantId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);
}
