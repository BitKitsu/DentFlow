package pl.edu.ur.dentflow.core.reservation.api;

import pl.edu.ur.dentflow.core.clinic.domain.Tenant;
import pl.edu.ur.dentflow.core.clinic.infrastructure.TenantRepository;
import pl.edu.ur.dentflow.core.reservation.application.RoomOccupancyReportService;
import pl.edu.ur.dentflow.pdf.DentFlowPdfGenerator;
import pl.edu.ur.dentflow.pdf.model.RoomOccupancyReportData;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/tenants/{tenantId}/reports/room-occupancy")
public class RoomOccupancyReportController {

    private final RoomOccupancyReportService reportService;
    private final TenantRepository tenantRepository;
    private final DentFlowPdfGenerator pdfGenerator = new DentFlowPdfGenerator();

    public RoomOccupancyReportController(RoomOccupancyReportService reportService,
                                          TenantRepository tenantRepository) {
        this.reportService = reportService;
        this.tenantRepository = tenantRepository;
    }

    @GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getRoomOccupancyReport(
            @PathVariable Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        String clinicName = getClinicName(tenantId);
        OffsetDateTime fromDt = from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toDt = to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        RoomOccupancyReportData data = reportService.buildReportData(tenantId, clinicName, fromDt, toDt, "Wszystkie gabinety");

        try {
            byte[] pdf = pdfGenerator.generateRoomOccupancy(data);
            String filename = "raport_oblozenia_" + from + "_" + to + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(pdf);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Błąd generowania PDF: " + e.getMessage());
        }
    }

    @GetMapping(value = "/{roomId}", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getRoomOccupancy(
            @PathVariable Long tenantId,
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        String clinicName = getClinicName(tenantId);
        OffsetDateTime fromDt = from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toDt = to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        RoomOccupancyReportData data = reportService.buildSingleRoomReportData(tenantId, clinicName, roomId, fromDt, toDt);

        try {
            byte[] pdf = pdfGenerator.generateRoomOccupancy(data);
            String filename = "raport_oblozenia_gabinet_" + roomId + "_" + from + "_" + to + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(pdf);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Błąd generowania PDF: " + e.getMessage());
        }
    }

    private String getClinicName(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .map(Tenant::getName)
                .orElse("Gabinet");
    }
}
