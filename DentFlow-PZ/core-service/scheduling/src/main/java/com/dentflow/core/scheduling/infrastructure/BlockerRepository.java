package com.dentflow.core.scheduling.infrastructure;

import com.dentflow.core.scheduling.domain.Blocker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface BlockerRepository extends JpaRepository<Blocker, Long> {

    List<Blocker> findByTenantId(Long tenantId);

    @Query("SELECT b FROM Blocker b WHERE b.tenantId = :tenantId " +
           "AND (b.staffId = :staffId OR b.roomId = :roomId) " +
           "AND b.startAt < :end AND b.endAt > :start")
    List<Blocker> findConflicting(
            @Param("tenantId") Long tenantId,
            @Param("staffId") Long staffId,
            @Param("roomId") Long roomId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end);
}
