package pl.edu.ur.dentflow.core.clinic.infrastructure;

import pl.edu.ur.dentflow.core.clinic.domain.StaffMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffMemberRepository extends JpaRepository<StaffMember, Long> {

    @Query("SELECT s FROM StaffMember s WHERE s.tenant.id = :tenantId")
    List<StaffMember> findByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT s FROM StaffMember s WHERE s.id = :id AND s.tenant.id = :tenantId")
    Optional<StaffMember> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    List<StaffMember> findByUserId(Long userId);
}
