package pl.edu.ur.dentflow.core.clinic.infrastructure;

import pl.edu.ur.dentflow.core.clinic.domain.StaffWorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffWorkingHoursRepository extends JpaRepository<StaffWorkingHours, Long> {

    List<StaffWorkingHours> findByStaffMemberIdOrderByDayOfWeekAsc(Long staffMemberId);

    void deleteByStaffMemberId(Long staffMemberId);
}
