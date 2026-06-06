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

@RestController
@RequestMapping("/tenants/{tenantId}/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DOCTOR')")
    public ResponseEntity<List<AppointmentResponse>> getAppointments(
            @PathVariable Long tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ResponseEntity.ok(appointmentService.getAppointments(tenantId, from, to));
    }

    /**
     * Endpoint dla zalogowanego pacjenta — zwraca wyłącznie jego własne wizyty.
     * Dostępny dla każdego uwierzytelnionego użytkownika.
     * userId jest wyciągany z tokenu JWT (claim "userId") przekazanego jako credentials.
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
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DOCTOR')")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(tenantId, request));
    }

    @PutMapping("/{appointmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DOCTOR')")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable Long tenantId,
            @PathVariable Long appointmentId,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.updateAppointment(tenantId, appointmentId, request));
    }

    @PostMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DOCTOR')")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable Long tenantId,
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(tenantId, appointmentId));
    }

    @PostMapping("/{appointmentId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DOCTOR')")
    public ResponseEntity<AppointmentResponse> completeAppointment(
            @PathVariable Long tenantId,
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.completeAppointment(tenantId, appointmentId));
    }
}
