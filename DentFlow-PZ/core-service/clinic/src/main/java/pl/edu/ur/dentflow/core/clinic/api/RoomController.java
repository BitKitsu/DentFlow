package pl.edu.ur.dentflow.core.clinic.api;

import pl.edu.ur.dentflow.core.clinic.application.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller managing dental rooms in the DentFlow system.
 *
 * <p>Rooms represent physical treatment rooms within a clinic location.
 * Staff members (dentists) can be assigned to rooms to indicate which
 * rooms they typically work in.</p>
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /tenants/{tenantId}/rooms - list rooms</li>
 *   <li>POST /tenants/{tenantId}/rooms - create a room (OWNER, RECEPTIONIST)</li>
 *   <li>PUT /tenants/{tenantId}/rooms/{roomId} - update a room (OWNER, RECEPTIONIST)</li>
 *   <li>DELETE /tenants/{tenantId}/rooms/{roomId} - delete a room (OWNER only)</li>
 *   <li>POST /tenants/{tenantId}/rooms/{roomId}/staff/{staffId} - assign staff</li>
 *   <li>DELETE /tenants/{tenantId}/rooms/{roomId}/staff/{staffId} - remove staff</li>
 * </ul>
 *
 * @see pl.edu.ur.dentflow.core.clinic.application.RoomService
 */
@RestController
@RequestMapping("/tenants/{tenantId}/rooms")
@Tag(name = "Rooms", description = "Room management in clinic")
@SecurityRequirement(name = "bearerAuth")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    @Operation(summary = "List rooms in clinic")
    public ResponseEntity<List<RoomResponse>> getRooms(@PathVariable Long tenantId) {
        return ResponseEntity.ok(roomService.getRooms(tenantId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST')")
    @Operation(summary = "Create a new room")
    public ResponseEntity<RoomResponse> createRoom(
            @PathVariable Long tenantId,
            @RequestParam String name,
            @RequestParam Long locationId) {
        return ResponseEntity.ok(roomService.createRoom(tenantId, name, locationId));
    }

    @PutMapping("/{roomId}")
    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST')")
    @Operation(summary = "Update a room")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long tenantId,
            @PathVariable Long roomId,
            @RequestParam String name,
            @RequestParam Long locationId) {
        return ResponseEntity.ok(roomService.updateRoom(tenantId, roomId, name, locationId));
    }

    @DeleteMapping("/{roomId}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Delete a room")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long tenantId,
            @PathVariable Long roomId) {
        roomService.deleteRoom(tenantId, roomId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roomId}/staff/{staffId}")
    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST')")
    @Operation(summary = "Assign a staff member to a room")
    public ResponseEntity<Void> assignStaff(
            @PathVariable Long tenantId,
            @PathVariable Long roomId,
            @PathVariable Long staffId) {
        roomService.assignStaff(tenantId, roomId, staffId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roomId}/staff/{staffId}")
    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST')")
    @Operation(summary = "Remove a staff member from a room")
    public ResponseEntity<Void> removeStaff(
            @PathVariable Long tenantId,
            @PathVariable Long roomId,
            @PathVariable Long staffId) {
        roomService.removeStaff(tenantId, roomId, staffId);
        return ResponseEntity.noContent().build();
    }
}
