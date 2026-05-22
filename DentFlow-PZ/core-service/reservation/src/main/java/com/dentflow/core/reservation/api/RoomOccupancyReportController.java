package com.dentflow.core.reservation.api;

import com.dentflow.core.reservation.application.RoomOccupancyReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dentflow.pdf.DentFlowPdfGenerator;
import com.dentflow.pdf.model.RoomOccupancyReportData;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/tenants/{tenantId}/reports/room-occupancy")
public class RoomOccupancyReportController {

    private final RoomOccupancyReportService reportService;

    public RoomOccupancyReportController(RoomOccupancyReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * GET /tenants/{tenantId}/reports/room-occupancy?from=...&to=...
     * Zwraca raport obłożenia wszystkich gabinetów w formacie PDF.
     */
    @GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getRoomOccupancyReport(
            @PathVariable Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        
        List<RoomOccupancyDTO> stats = reportService.getRoomOccupancyReport(tenantId, from, to);
        
        // Mocking advanced statistics required by PDF Generator as they are not yet fully implemented in service
        List<RoomOccupancyReportData.DailyStats> dailyStats = List.of(
            new RoomOccupancyReportData.DailyStats(1, stats.size() * 2L),
            new RoomOccupancyReportData.DailyStats(2, stats.size() * 3L)
        );
        List<RoomOccupancyReportData.DoctorStats> doctorStats = List.of(
            new RoomOccupancyReportData.DoctorStats("Dr Kowalski", stats.size() * 5L, 40.0, 85.0)
        );
        
        RoomOccupancyReportData data = new RoomOccupancyReportData(
                "DentFlow Clinic",
                from.getMonthValue(),
                from.getYear(),
                "Wszystkie gabinety",
                dailyStats,
                doctorStats,
                30.0,
                List.of("Przegląd", "Wypełnienie"),
                5.0
        );

        try {
            byte[] pdf = new DentFlowPdfGenerator().generateRoomOccupancy(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"raport_oblozenia.pdf\"")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /tenants/{tenantId}/reports/room-occupancy/{roomId}?from=...&to=...
     * Zwraca raport obłożenia konkretnego gabinetu w formacie PDF.
     */
    @GetMapping(value = "/{roomId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getRoomOccupancy(
            @PathVariable Long tenantId,
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        
        RoomOccupancyDTO stat = reportService.getRoomOccupancy(tenantId, roomId, from, to);

        RoomOccupancyReportData data = new RoomOccupancyReportData(
                "DentFlow Clinic",
                from.getMonthValue(),
                from.getYear(),
                "Gabinet " + roomId,
                List.of(new RoomOccupancyReportData.DailyStats(1, stat.appointmentCount())),
                List.of(),
                30.0,
                List.of(),
                0.0
        );

        try {
            byte[] pdf = new DentFlowPdfGenerator().generateRoomOccupancy(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"raport_oblozenia_gabinet_" + roomId + ".pdf\"")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
