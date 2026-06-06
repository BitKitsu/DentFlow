package pl.edu.ur.dentflow.core.clinic.infrastructure;

import pl.edu.ur.dentflow.core.clinic.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByTenantId(Long tenantId);
    List<Room> findByLocationId(Long locationId);
    Optional<Room> findByIdAndTenantId(Long id, Long tenantId);
}
