package pl.edu.ur.dentflow.pdf.generator;

import pl.edu.ur.dentflow.pdf.model.PatientVisitHistoryReportData;
import pl.edu.ur.dentflow.pdf.util.PdfStyles;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * PDF generator for Report 3: Patient Visit History.
 */
public class PatientVisitHistoryPdfGenerator {

    /**
     * Generates the report as a byte array.
     *
     * @param data report data from backend
     * @return PDF file bytes
     * @throws IOException if write error occurs
     */
    public byte[] generate(PatientVisitHistoryReportData data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc)) {

            document.setMargins(40, 40, 40, 40);

            // Header
            document.add(PdfStyles.reportTitle("Report: Patient Visit History"));
            document.add(PdfStyles.reportSubtitle(
                    data.clinicName() + "  |  Zakres dat: " + data.dateRangeDescription()));

            // Patient data
            document.add(PdfStyles.sectionTitle("Patient Data"));
            document.add(PdfStyles.infoLine("Full name",
                    data.patientFirstName() + " " + data.patientLastName()));
            if (data.patientPhone() != null && !data.patientPhone().isBlank()) {
                document.add(PdfStyles.infoLine("Phone", data.patientPhone()));
            }
            if (data.patientEmail() != null && !data.patientEmail().isBlank()) {
                document.add(PdfStyles.infoLine("E-mail", data.patientEmail()));
            }

            // Visit history table
            document.add(PdfStyles.sectionTitle("Visit History"));

            Table table = PdfStyles.createTable(
                    "Date", "Doctor", "Service", "Status", "Notes");

            List<PatientVisitHistoryReportData.VisitRow> visits = data.visits();
            for (int i = 0; i < visits.size(); i++) {
                PatientVisitHistoryReportData.VisitRow v = visits.get(i);
                PdfStyles.addRow(table, i,
                        v.date(),
                        v.doctorFullName(),
                        v.serviceName(),
                        v.status(),
                        v.notes());
            }
            document.add(table);

            // Summary
            document.add(new Paragraph("\n"));
            document.add(PdfStyles.summaryLine("Summary"));
            document.add(PdfStyles.infoLine("Total visits", String.valueOf(visits.size())));

            visits.stream()
                    .map(PatientVisitHistoryReportData.VisitRow::date)
                    .max(String::compareTo)
                    .ifPresent(lastDate -> document.add(PdfStyles.infoLine("Last visit", lastDate)));
        }

        return baos.toByteArray();
    }
}
