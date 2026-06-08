package pl.edu.ur.dentflow.core.clinic.api;

import pl.edu.ur.dentflow.core.clinic.domain.Room;

import java.util.Collections;
import java.util.List;

public record RoomResponse(
        Long id,
        Long tenantId,
        Long locationId,
        String name,
        List<Long> assignedStaffIds
) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getTenant().getId(),
                room.getLocation().getId(),
                room.getName(),
                Collections.emptyList()
        );
    }

    public static RoomResponse from(Room room, List<Long> assignedStaffIds) {
        return new RoomResponse(
                room.getId(),
                room.getTenant().getId(),
                room.getLocation().getId(),
                room.getName(),
                assignedStaffIds
        );
    }
}
