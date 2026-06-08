package pl.edu.ur.dentflow.pdf.generator;

import pl.edu.ur.dentflow.pdf.model.AppointmentListReportData;
import pl.edu.ur.dentflow.pdf.util.PdfStyles;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF generator for Report 1: Appointment List.
 *
 * Usage:
 * 
 * <pre>
 * byte[] pdf = new AppointmentListPdfGenerator().generate(data);
 * </pre>
 */
public class AppointmentListPdfGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /**
     * Generates the report as a byte array ready for HTTP response.
     *
     * @param data report data from backend
     * @return PDF file bytes
     * @throws IOException if write error occurs
     */
    public byte[] generate(AppointmentListReportData data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc)) {

            document.setMargins(40, 40, 40, 40);

            // Header
            document.add(PdfStyles.reportTitle("Report: Appointment List"));
            document.add(PdfStyles.reportSubtitle(
                    data.clinicName() + "  |  " +
                            data.dateFrom().format(DATE_FMT) + " – " + data.dateTo().format(DATE_FMT)));

            // Filtering parameters
            if (data.doctorFilter() != null) {
                document.add(PdfStyles.infoLine("Doctor", data.doctorFilter()));
            }
            if (data.locationFilter() != null) {
                document.add(PdfStyles.infoLine("Location", data.locationFilter()));
            }
            if (data.statusFilter() != null) {
                document.add(PdfStyles.infoLine("Status", data.statusFilter()));
            }

            // Appointments table
            document.add(PdfStyles.sectionTitle("Appointment List"));

            List<AppointmentListReportData.AppointmentRow> rows = data.appointments();
            if (rows.isEmpty()) {
                document.add(new Paragraph("Brak wizyt w wybranym zakresie dat.")
                        .setFont(PdfStyles.fontRegular()).setFontSize(11));
            } else {
                Table table = PdfStyles.createTable(
                        "Date / Time", "Patient", "Doctor", "Service", "Status");

                for (int i = 0; i < rows.size(); i++) {
                    AppointmentListReportData.AppointmentRow row = rows.get(i);
                    PdfStyles.addRow(table, i,
                            row.dateTime(),
                            row.patientFullName(),
                            row.doctorFullName(),
                            row.serviceName(),
                            row.status());
                }
                document.add(table);

                // Summary
                long cancelled = rows.stream()
                        .filter(r -> "CANCELLED".equalsIgnoreCase(r.status())).count();
                long noShow = rows.stream()
                        .filter(r -> "NO_SHOW".equalsIgnoreCase(r.status())).count();

                document.add(new Paragraph("\n"));
                document.add(PdfStyles.summaryLine("Summary"));
                document.add(PdfStyles.infoLine("Total appointments", String.valueOf(rows.size())));
                document.add(PdfStyles.infoLine("Cancelled", String.valueOf(cancelled)));
                document.add(PdfStyles.infoLine("No-show", String.valueOf(noShow)));
            }
        }

        return baos.toByteArray();
    }
}
