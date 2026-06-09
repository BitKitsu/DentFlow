package pl.edu.ur.dentflow.pdf;

import pl.edu.ur.dentflow.pdf.generator.AppointmentListPdfGenerator;
import pl.edu.ur.dentflow.pdf.generator.PatientVisitHistoryPdfGenerator;
import pl.edu.ur.dentflow.pdf.generator.RoomOccupancyPdfGenerator;
import pl.edu.ur.dentflow.pdf.model.AppointmentListReportData;
import pl.edu.ur.dentflow.pdf.model.PatientVisitHistoryReportData;
import pl.edu.ur.dentflow.pdf.model.RoomOccupancyReportData;

import java.io.IOException;

/**
 * Main API of the DentFlow PDF Generator library.
 *
 * Usage in Spring Boot (after adding JAR to backend):
 * <pre>{@code
 *   DentFlowPdfGenerator pdf = new DentFlowPdfGenerator();
 *
 *   // Report 1: appointment list
 *   byte[] bytes = pdf.generateAppointmentList(data);
 *
 *   // Report 2: room occupancy
 *   byte[] bytes = pdf.generateRoomOccupancy(data);
 *
 *   // Report 3: patient visit history
 *   byte[] bytes = pdf.generatePatientHistory(data);
 *
 *   // Return via controller:
 *   return ResponseEntity.ok()
 *       .contentType(MediaType.APPLICATION_PDF)
 *       .header("Content-Disposition", "attachment; filename=\"report.pdf\"")
 *       .body(bytes);
 * }</pre>
 */
public class DentFlowPdfGenerator {

    private final AppointmentListPdfGenerator appointmentListGenerator;
    private final RoomOccupancyPdfGenerator roomOccupancyGenerator;
    private final PatientVisitHistoryPdfGenerator patientHistoryGenerator;

    public DentFlowPdfGenerator() {
        this.appointmentListGenerator = new AppointmentListPdfGenerator();
        this.roomOccupancyGenerator   = new RoomOccupancyPdfGenerator();
        this.patientHistoryGenerator  = new PatientVisitHistoryPdfGenerator();
    }

    /**
     * Generates PDF with appointment list (Report 1).
     *
     * @param data report parameters and data
     * @return PDF bytes
     * @throws IOException generation error
     */
    public byte[] generateAppointmentList(AppointmentListReportData data) throws IOException {
        return appointmentListGenerator.generate(data);
    }

    /**
     * Generates PDF with room occupancy report (Report 2).
     *
     * @param data report parameters and data
     * @return PDF bytes
     * @throws IOException generation error
     */
    public byte[] generateRoomOccupancy(RoomOccupancyReportData data) throws IOException {
        return roomOccupancyGenerator.generate(data);
    }

    /**
     * Generates PDF with patient visit history (Report 3).
     *
     * @param data report parameters and data
     * @return PDF bytes
     * @throws IOException generation error
     */
    public byte[] generatePatientHistory(PatientVisitHistoryReportData data) throws IOException {
        return patientHistoryGenerator.generate(data);
    }
}
