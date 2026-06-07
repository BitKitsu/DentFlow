package pl.edu.ur.dentflow.core.reservation.api;

import pl.edu.ur.dentflow.core.reservation.application.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * REST controller managing appointments (reservations) in the DentFlow system.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /tenants/{tenantId}/appointments - list appointments (optional date filter)</li>
 *   <li>GET /tenants/{tenantId}/appointments/my - logged-in user's appointments</li>
 *   <li>GET /tenants/{tenantId}/appointments/{id} - appointment details</li>
 *   <li>POST /tenants/{tenantId}/appointments - create new appointment</li>
 *   <li>PUT /tenants/{tenantId}/appointments/{id} - update appointment</li>
 *   <li>POST /tenants/{tenantId}/appointments/{id}/cancel - cancel appointment</li>
 *   <li>POST /tenants/{tenantId}/appointments/{id}/complete - mark as completed</li>
 * </ul>
 *
 * <p>Access: OWNER, DENTIST, RECEPTIONIST (management) or isAuthenticated (own appointments).</p>
 *
 * @see pl.edu.ur.dentflow.core.reservation.application.AppointmentService
 */
@RestController
@RequestMapping("/tenants/{tenantId}/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'DENTIST', 'RECEPTIONIST', 'ASSISTANT')")
    public ResponseEntity<List<AppointmentResponse>> getAppointments(
            @PathVariable Long tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ResponseEntity.ok(appointmentService.getAppointments(tenantId, from, to));
    }

    /**
     * Endpoint for logged-in patient - returns only their own appointments.
     * Available to any authenticated user.
     * userId is extracted from the JWT token (claim "userId") passed as credentials.
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(
            @PathVariable Long tenantId,
            Authentication authentication) {
        Long userId = authentication.getCredentials() instanceof Long
                ? (Long) authentication.getCredentials()
                : 0L;
        return ResponseEntity.ok(appointmentService.getMyAppointments(tenantId, userId));
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> getAppointment(
            @PathVariable Long tenantId,
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.getAppointment(tenantId, appointmentId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'DENTIST', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(tenantId, request));
    }

    @PutMapping("/{appointmentId}")
    @PreAuthorize("hasAnyRole('OWNER', 'DENTIST', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable Long tenantId,
            @PathVariable Long appointmentId,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.updateAppointment(tenantId, appointmentId, request));
    }

    @PostMapping("/{appointmentId}/confirm")
    @PreAuthorize("hasAnyRole('OWNER', 'DENTIST', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> confirmAppointment(
            @PathVariable Long tenantId,
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.confirmAppointment(tenantId, appointmentId));
    }

    @PostMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasAnyRole('OWNER', 'DENTIST', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable Long tenantId,
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(tenantId, appointmentId));
    }

    @PostMapping("/{appointmentId}/complete")
    @PreAuthorize("hasAnyRole('OWNER', 'DENTIST', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> completeAppointment(
            @PathVariable Long tenantId,
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.completeAppointment(tenantId, appointmentId));
    }
}
