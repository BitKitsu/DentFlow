package pl.edu.ur.dentflow.core.reservation.application;

import pl.edu.ur.dentflow.core.clinic.domain.StaffMember;
import pl.edu.ur.dentflow.core.clinic.infrastructure.StaffMemberRepository;
import pl.edu.ur.dentflow.core.patient.domain.Patient;
import pl.edu.ur.dentflow.core.patient.infrastructure.PatientRepository;
import pl.edu.ur.dentflow.core.reservation.api.PatientVisitHistoryDTO;
import pl.edu.ur.dentflow.core.reservation.domain.Appointment;
import pl.edu.ur.dentflow.core.reservation.infrastructure.AppointmentRepository;
import pl.edu.ur.dentflow.pdf.DentFlowPdfGenerator;
import pl.edu.ur.dentflow.pdf.model.PatientVisitHistoryReportData;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class PatientVisitHistoryService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final StaffMemberRepository staffMemberRepository;
    private final DentFlowPdfGenerator pdfGenerator = new DentFlowPdfGenerator();

    public PatientVisitHistoryService(AppointmentRepository appointmentRepository,
                                      PatientRepository patientRepository,
                                      StaffMemberRepository staffMemberRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.staffMemberRepository = staffMemberRepository;
    }

    @Transactional(readOnly = true)
    public List<PatientVisitHistoryDTO> getPatientHistory(Long tenantId, Long patientId,
                                                          LocalDate from, LocalDate to) {
        List<Appointment> appointments =
                appointmentRepository.findByTenantIdAndPatientIdOrderByStartAtDesc(tenantId, patientId);

        if (from != null && to != null) {
            OffsetDateTime fromDt = from.atStartOfDay().atOffset(ZoneOffset.UTC);
            OffsetDateTime toDt = to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
            appointments = appointments.stream()
                    .filter(a -> !a.getStartAt().isBefore(fromDt) && a.getStartAt().isBefore(toDt))
                    .toList();
        }

        return appointments.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<PatientVisitHistoryDTO> getPatientHistoryByStatus(Long tenantId, Long patientId,
                                                                   String status, LocalDate from, LocalDate to) {
        return getPatientHistory(tenantId, patientId, from, to)
                .stream()
                .filter(v -> v.status().equalsIgnoreCase(status))
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] generatePdf(Long tenantId, Long patientId, String clinicName, String statusFilter,
                              LocalDate from, LocalDate to) {
        Patient patient = patientRepository.findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pacjent o id=" + patientId + " nie istnieje w tym gabinecie"));

        List<PatientVisitHistoryDTO> visits = (statusFilter != null && !statusFilter.isBlank())
                ? getPatientHistoryByStatus(tenantId, patientId, statusFilter, from, to)
                : getPatientHistory(tenantId, patientId, from, to);

        if (visits.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Brak wizyt dla pacjenta o id=" + patientId);
        }

        List<PatientVisitHistoryReportData.VisitRow> rows = visits.stream()
                .map(dto -> {
                    String dentistName = dto.dentistStaffId() != null
                            ? staffMemberRepository.findById(dto.dentistStaffId())
                                    .map(s -> s.getFirstName() + " " + s.getLastName())
                                    .orElse("Dentist ID: " + dto.dentistStaffId())
                            : "No dentist";
                    String serviceName = dto.serviceItemId() != null
                            ? "Service ID: " + dto.serviceItemId()
                            : "";
                    return new PatientVisitHistoryReportData.VisitRow(
                            dto.startAt().toString(),
                            dentistName,
                            serviceName,
                            dto.status(),
                            dto.notes()
                    );
                }).toList();

        PatientVisitHistoryReportData data = new PatientVisitHistoryReportData(
                clinicName,
                patient.getFirstName(),
                patient.getLastName(),
                patient.getPhone() != null ? patient.getPhone() : "",
                patient.getEmail() != null ? patient.getEmail() : "",
                (statusFilter != null && !statusFilter.isBlank()) ? "Status: " + statusFilter : "All",
                rows
        );

        try {
            return pdfGenerator.generatePatientHistory(data);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "PDF generation error: " + e.getMessage());
        }
    }

    private PatientVisitHistoryDTO toDTO(Appointment a) {
        return new PatientVisitHistoryDTO(
                a.getId(),
                a.getTenantId(),
                a.getLocationId(),
                a.getRoomId(),
                a.getDentistStaffId(),
                a.getServiceItemId(),
                a.getStartAt(),
                a.getEndAt(),
                a.getStatus(),
                a.getNotes()
        );
    }
}
