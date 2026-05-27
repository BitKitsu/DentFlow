package com.dentflow.pdf;

import com.dentflow.pdf.model.AppointmentListReportData;
import com.dentflow.pdf.model.PatientVisitHistoryReportData;
import com.dentflow.pdf.model.RoomOccupancyReportData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DentFlowPdfGeneratorTest {

    private final DentFlowPdfGenerator generator = new DentFlowPdfGenerator();

    @Test
    void generateAppointmentList_returnsNonEmptyPdf() throws IOException {
        AppointmentListReportData data = new AppointmentListReportData(
                "DentCare Kraków",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                null, null, null,
                List.of(
                        new AppointmentListReportData.AppointmentRow(
                                "20.04.2026 08:00", "Tomasz Lewandowski",
                                "dr Jan Kowalski", "Przegląd stomatologiczny", "COMPLETED"),
                        new AppointmentListReportData.AppointmentRow(
                                "20.04.2026 09:00", "Katarzyna Wójcik",
                                "dr Jan Kowalski", "Wypełnienie kompozytowe", "COMPLETED"),
                        new AppointmentListReportData.AppointmentRow(
                                "20.04.2026 11:00", "Paweł Dąbrowski",
                                "dr Anna Nowak", "Przegląd stomatologiczny", "NO_SHOW")
                )
        );

        byte[] pdf = generator.generateAppointmentList(data);

        assertNotNull(pdf);
        assertTrue(pdf.length > 1000, "PDF powinien mieć więcej niż 1kB");
        // Sprawdzamy nagłówek pliku PDF (%PDF-)
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
    }

    @Test
    void generateRoomOccupancy_returnsNonEmptyPdf() throws IOException {
        RoomOccupancyReportData data = new RoomOccupancyReportData(
                "DentCare Kraków",
                4, 2026,
                null,
                List.of(
                        new RoomOccupancyReportData.DailyStats(20, 5),
                        new RoomOccupancyReportData.DailyStats(21, 4),
                        new RoomOccupancyReportData.DailyStats(22, 2),
                        new RoomOccupancyReportData.DailyStats(23, 3),
                        new RoomOccupancyReportData.DailyStats(24, 1)
                ),
                List.of(
                        new RoomOccupancyReportData.DoctorStats("dr Jan Kowalski", 8, 6.5, 81.25),
                        new RoomOccupancyReportData.DoctorStats("dr Anna Nowak", 7, 5.5, 68.75)
                ),
                42.0,
                List.of("Przegląd stomatologiczny", "Wypełnienie kompozytowe", "Skaling"),
                8.3
        );

        byte[] pdf = generator.generateRoomOccupancy(data);

        assertNotNull(pdf);
        assertTrue(pdf.length > 1000);
        assertEquals('%', (char) pdf[0]);
    }

    @Test
    void generatePatientHistory_returnsNonEmptyPdf() throws IOException {
        PatientVisitHistoryReportData data = new PatientVisitHistoryReportData(
                "DentCare Kraków",
                "Tomasz", "Lewandowski",
                "+48 600 111 222", "pacjent1@gmail.com",
                "Wszystkie",
                List.of(
                        new PatientVisitHistoryReportData.VisitRow(
                                "20.04.2026 08:00", "dr Jan Kowalski",
                                "Przegląd stomatologiczny", "COMPLETED", "Przegląd ok, zalecany skaling"),
                        new PatientVisitHistoryReportData.VisitRow(
                                "21.04.2026 09:30", "dr Jan Kowalski",
                                "Skaling", "SCHEDULED", null)
                )
        );

        byte[] pdf = generator.generatePatientHistory(data);

        assertNotNull(pdf);
        assertTrue(pdf.length > 1000);
        assertEquals('%', (char) pdf[0]);
    }
}
