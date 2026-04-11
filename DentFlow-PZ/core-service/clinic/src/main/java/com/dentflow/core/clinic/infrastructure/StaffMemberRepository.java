package com.dentflow.core.clinic.infrastructure;

import com.dentflow.core.clinic.domain.StaffMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffMemberRepository extends JpaRepository<StaffMember, Long> {
    List<StaffMember> findByTenantId(Long tenantId);
    Optional<StaffMember> findByIdAndTenantId(Long id, Long tenantId);
}
