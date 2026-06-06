package pl.edu.ur.dentflow.core.reservation.application;

import pl.edu.ur.dentflow.core.clinic.domain.Tenant;
import pl.edu.ur.dentflow.core.clinic.infrastructure.TenantRepository;
import pl.edu.ur.dentflow.core.reservation.infrastructure.AppointmentDetailsProjection;
import pl.edu.ur.dentflow.core.reservation.infrastructure.AppointmentRepository;
import pl.edu.ur.dentflow.pdf.DentFlowPdfGenerator;
import pl.edu.ur.dentflow.pdf.model.AppointmentListReportData;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class AppointmentListReportService {

    private final AppointmentRepository appointmentRepository;
    private final TenantRepository tenantRepository;
    private final DentFlowPdfGenerator pdfGenerator = new DentFlowPdfGenerator();

    public AppointmentListReportService(AppointmentRepository appointmentRepository,
                                         TenantRepository tenantRepository) {
        this.appointmentRepository = appointmentRepository;
        this.tenantRepository = tenantRepository;
    }

    public byte[] generateReport(Long tenantId, LocalDate from, LocalDate to,
                                 String status, Long dentistId) {
        if (to.isBefore(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Parametr 'to' musi być >= 'from'");
        }

        String clinicName = tenantRepository.findById(tenantId)
                .map(Tenant::getName)
                .orElse("Gabinet");

        OffsetDateTime fromDt = from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toDt   = to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        List<AppointmentDetailsProjection> rows = appointmentRepository
                .searchAppointmentsDetails(tenantId, status, dentistId, null, fromDt, toDt);

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
                clinicName,
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
