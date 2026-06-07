package pl.edu.ur.dentflow.pdf.generator;

import pl.edu.ur.dentflow.pdf.model.RoomOccupancyReportData;
import pl.edu.ur.dentflow.pdf.util.PdfStyles;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * PDF generator for Report 2: Room Occupancy.
 */
public class RoomOccupancyPdfGenerator {

        private static final DecimalFormat DF = new DecimalFormat("0.0");

        /**
         * Generates the report as a byte array.
         *
         * @param data report data from backend
         * @return PDF file bytes
         * @throws IOException if write error occurs
         */
        public byte[] generate(RoomOccupancyReportData data) throws IOException {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                String monthName = Month.of(data.month())
                                .getDisplayName(TextStyle.FULL_STANDALONE, new Locale("pl"));
                String period = monthName + " " + data.year();

                try (PdfWriter writer = new PdfWriter(baos);
                                PdfDocument pdfDoc = new PdfDocument(writer);
                                Document document = new Document(pdfDoc)) {

                        document.setMargins(40, 40, 40, 40);

                        // Header
                        document.add(PdfStyles.reportTitle("Report: Room Occupancy"));
                        document.add(PdfStyles.reportSubtitle(
                                        data.clinicName() + "  |  " + period +
                                                        (data.locationFilter() != null ? "  |  " + data.locationFilter()
                                                                        : "")));

                        // Bar chart (text-based)
                        document.add(PdfStyles.sectionTitle("Daily Appointment Count"));

                        if (data.dailyStats().isEmpty() && data.doctorStats().isEmpty()) {
                                document.add(new Paragraph("Brak wizyt w wybranym zakresie dat.")
                                                .setFont(PdfStyles.fontRegular()).setFontSize(11));
                        } else {
                                long maxCount = data.dailyStats().stream()
                                                .mapToLong(RoomOccupancyReportData.DailyStats::appointmentCount)
                                                .max().orElse(1);

                                Table barTable = new Table(UnitValue.createPercentArray(new float[] { 8, 12, 80 }))
                                                .useAllAvailableWidth()
                                                .setMarginTop(4);

                                for (RoomOccupancyReportData.DailyStats ds : data.dailyStats()) {
                                        int barLength = (int) (ds.appointmentCount() * 40 / maxCount);
                                        String bar = "█".repeat(Math.max(barLength, 1));
                                        barTable.addCell(cellText(String.valueOf(ds.dayOfMonth())));
                                        barTable.addCell(cellText(String.valueOf(ds.appointmentCount())));
                                        barTable.addCell(cellText(bar));
                                }
                                document.add(barTable);

                                // Doctor statistics table
                                document.add(PdfStyles.sectionTitle("Staff Statistics"));

                                Table table = PdfStyles.createTable(
                                                "Doctor", "Appointments", "Working hours", "% Slots");

                                List<RoomOccupancyReportData.DoctorStats> doctors = data.doctorStats();
                                for (int i = 0; i < doctors.size(); i++) {
                                        RoomOccupancyReportData.DoctorStats d = doctors.get(i);
                                        PdfStyles.addRow(table, i,
                                                        d.doctorFullName(),
                                                        String.valueOf(d.appointmentCount()),
                                                        DF.format(d.workHours()) + " h",
                                                        DF.format(d.slotUtilizationPercent()) + "%");
                                }
                                document.add(table);

                                // General statistics
                                document.add(PdfStyles.sectionTitle("General Statistics"));
                                document.add(PdfStyles.infoLine("Average appointment duration",
                                                DF.format(data.avgAppointmentMinutes()) + " min"));
                                document.add(PdfStyles.infoLine("No-show rate",
                                                DF.format(data.noShowRate()) + "%"));

                                if (data.topServices() != null && !data.topServices().isEmpty()) {
                                        document.add(PdfStyles.infoLine("Most popular services",
                                                        String.join(", ", data.topServices())));
                                }
                        }
                }

                return baos.toByteArray();
        }

        private com.itextpdf.layout.element.Cell cellText(String text) {
                return new com.itextpdf.layout.element.Cell()
                                .add(new Paragraph(text).setFont(PdfStyles.fontRegular()).setFontSize(9))
                                .setPadding(4);
        }
}
