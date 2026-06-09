package pl.edu.ur.dentflow.core.clinic.infrastructure;

import pl.edu.ur.dentflow.core.clinic.domain.StaffRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRoomRepository extends JpaRepository<StaffRoom, Long> {

    List<StaffRoom> findByRoomId(Long roomId);

    List<StaffRoom> findByStaffMemberId(Long staffMemberId);

    Optional<StaffRoom> findByStaffMemberIdAndRoomId(Long staffMemberId, Long roomId);

    @Query("SELECT sr.room.id FROM StaffRoom sr WHERE sr.staffMember.id = :staffId")
    List<Long> findRoomIdsByStaffId(@Param("staffId") Long staffId);

    @Query("SELECT sr.staffMember.id FROM StaffRoom sr WHERE sr.room.id = :roomId")
    List<Long> findStaffIdsByRoomId(@Param("roomId") Long roomId);
}
