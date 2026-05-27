package com.dentflow.pdf;

import com.dentflow.pdf.generator.AppointmentListPdfGenerator;
import com.dentflow.pdf.generator.PatientVisitHistoryPdfGenerator;
import com.dentflow.pdf.generator.RoomOccupancyPdfGenerator;
import com.dentflow.pdf.model.AppointmentListReportData;
import com.dentflow.pdf.model.PatientVisitHistoryReportData;
import com.dentflow.pdf.model.RoomOccupancyReportData;

import java.io.IOException;

/**
 * Główne API biblioteki DentFlow PDF Generator.
 *
 * Użycie w Spring Boot (po dodaniu JAR-a do backendu):
 * <pre>{@code
 *   DentFlowPdfGenerator pdf = new DentFlowPdfGenerator();
 *
 *   // Raport 1: lista wizyt
 *   byte[] bytes = pdf.generateAppointmentList(data);
 *
 *   // Raport 2: obłożenie gabinetu
 *   byte[] bytes = pdf.generateRoomOccupancy(data);
 *
 *   // Raport 3: historia wizyt pacjenta
 *   byte[] bytes = pdf.generatePatientHistory(data);
 *
 *   // Zwrot przez kontroler:
 *   return ResponseEntity.ok()
 *       .contentType(MediaType.APPLICATION_PDF)
 *       .header("Content-Disposition", "attachment; filename=\"raport.pdf\"")
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
     * Generuje PDF z listą wizyt (Raport 1).
     *
     * @param data parametry i dane raportu
     * @return bajty PDF
     * @throws IOException błąd generowania
     */
    public byte[] generateAppointmentList(AppointmentListReportData data) throws IOException {
        return appointmentListGenerator.generate(data);
    }

    /**
     * Generuje PDF z raportem obłożenia gabinetu (Raport 2).
     *
     * @param data parametry i dane raportu
     * @return bajty PDF
     * @throws IOException błąd generowania
     */
    public byte[] generateRoomOccupancy(RoomOccupancyReportData data) throws IOException {
        return roomOccupancyGenerator.generate(data);
    }

    /**
     * Generuje PDF z historią wizyt pacjenta (Raport 3).
     *
     * @param data parametry i dane raportu
     * @return bajty PDF
     * @throws IOException błąd generowania
     */
    public byte[] generatePatientHistory(PatientVisitHistoryReportData data) throws IOException {
        return patientHistoryGenerator.generate(data);
    }
}
