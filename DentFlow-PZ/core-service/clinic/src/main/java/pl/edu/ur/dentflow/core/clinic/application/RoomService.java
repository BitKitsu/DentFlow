package pl.edu.ur.dentflow.core.clinic.application;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pl.edu.ur.dentflow.core.clinic.api.RoomResponse;
import pl.edu.ur.dentflow.core.clinic.domain.Room;
import pl.edu.ur.dentflow.core.clinic.domain.StaffMember;
import pl.edu.ur.dentflow.core.clinic.domain.StaffRoom;
import pl.edu.ur.dentflow.core.clinic.domain.Location;
import pl.edu.ur.dentflow.core.clinic.domain.Tenant;
import pl.edu.ur.dentflow.core.clinic.infrastructure.RoomRepository;
import pl.edu.ur.dentflow.core.clinic.infrastructure.StaffMemberRepository;
import pl.edu.ur.dentflow.core.clinic.infrastructure.StaffRoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final StaffRoomRepository staffRoomRepository;
    private final StaffMemberRepository staffMemberRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public RoomService(RoomRepository roomRepository,
                       StaffRoomRepository staffRoomRepository,
                       StaffMemberRepository staffMemberRepository) {
        this.roomRepository = roomRepository;
        this.staffRoomRepository = staffRoomRepository;
        this.staffMemberRepository = staffMemberRepository;
    }

    public List<RoomResponse> getRooms(Long tenantId) {
        return roomRepository.findByTenantId(tenantId).stream()
                .map(room -> {
                    List<Long> staffIds = staffRoomRepository.findStaffIdsByRoomId(room.getId());
                    return RoomResponse.from(room, staffIds);
                })
                .toList();
    }

    @Transactional
    public RoomResponse createRoom(Long tenantId, String name, Long locationId) {
        Tenant tenantRef = entityManager.getReference(Tenant.class, tenantId);
        Location locationRef = entityManager.getReference(Location.class, locationId);

        Room room = Room.builder()
                .tenant(tenantRef)
                .location(locationRef)
                .name(name)
                .build();
        Room saved = roomRepository.save(room);
        return RoomResponse.from(saved);
    }

    @Transactional
    public RoomResponse updateRoom(Long tenantId, Long roomId, String name, Long locationId) {
        Room room = roomRepository.findByIdAndTenantId(roomId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        room.setName(name);
        Location locationRef = entityManager.getReference(Location.class, locationId);
        room.setLocation(locationRef);
        Room saved = roomRepository.save(room);
        List<Long> staffIds = staffRoomRepository.findStaffIdsByRoomId(saved.getId());
        return RoomResponse.from(saved, staffIds);
    }

    public void deleteRoom(Long tenantId, Long roomId) {
        Room room = roomRepository.findByIdAndTenantId(roomId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        roomRepository.delete(room);
    }

    public void assignStaff(Long tenantId, Long roomId, Long staffId) {
        Room room = roomRepository.findByIdAndTenantId(roomId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        StaffMember staff = staffMemberRepository.findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found"));

        if (staffRoomRepository.findByStaffMemberIdAndRoomId(staffId, roomId).isPresent()) {
            return;
        }

        StaffRoom staffRoom = StaffRoom.builder()
                .staffMember(staff)
                .room(room)
                .build();
        staffRoomRepository.save(staffRoom);
    }

    public void removeStaff(Long tenantId, Long roomId, Long staffId) {
        staffRoomRepository.findByStaffMemberIdAndRoomId(staffId, roomId)
                .ifPresent(staffRoomRepository::delete);
    }
}
