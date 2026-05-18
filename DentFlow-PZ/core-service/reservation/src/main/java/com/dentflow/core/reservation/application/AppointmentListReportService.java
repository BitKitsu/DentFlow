package com.dentflow.core.reservation.application;

import com.dentflow.core.reservation.infrastructure.AppointmentDetailsProjection;
import com.dentflow.core.reservation.infrastructure.AppointmentRepository;
import com.dentflow.pdf.DentFlowPdfGenerator;
import com.dentflow.pdf.model.AppointmentListReportData;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Serwis generujący Raport 1: Lista wizyt.
 * SCRUM-60
 */
@Service
public class AppointmentListReportService {

    private final AppointmentRepository appointmentRepository;
    private final DentFlowPdfGenerator pdfGenerator;

    public AppointmentListReportService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
        this.pdfGenerator = new DentFlowPdfGenerator();
    }

    public byte[] generateReport(Long tenantId, LocalDate from, LocalDate to,
                                 String status, Long dentistId) {
        if (to.isBefore(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Parametr 'to' musi być >= 'from'");
        }

        OffsetDateTime fromDt = from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toDt   = to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        List<AppointmentDetailsProjection> rows = appointmentRepository
                .searchAppointmentsDetails(tenantId, status, dentistId, null)
                .stream()
                .filter(r -> !r.getStartAt().isBefore(fromDt) && r.getStartAt().isBefore(toDt))
                .toList();

        List<AppointmentListReportData.AppointmentRow> appointmentRows = rows.stream()
                .map(r -> new AppointmentListReportData.AppointmentRow(
                        r.getStartAt().toString(),
                        r.getPatientFirstName() + " " + r.getPatientLastName(),
                        r.getDentistName(),
                        r.getRoomName() != null ? r.getRoomName() : r.getLocationName(),
                        r.getStatus()
                ))
                .toList();

        AppointmentListReportData data = new AppointmentListReportData(
                "DentFlow Clinic",
                from,
                to,
                dentistId != null ? "Lekarz ID: " + dentistId : null,
                null,
                status,
                appointmentRows
        );

        try {
            return pdfGenerator.generateAppointmentList(data);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Błąd generowania PDF: " + e.getMessage());
        }
    }
}
