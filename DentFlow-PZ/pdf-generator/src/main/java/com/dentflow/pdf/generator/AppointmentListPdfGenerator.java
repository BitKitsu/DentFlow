package com.dentflow.pdf.generator;

import com.dentflow.pdf.model.AppointmentListReportData;
import com.dentflow.pdf.util.PdfStyles;
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
 * Generator PDF dla Raportu 1: Lista wizyt.
 *
 * Użycie:
 * 
 * <pre>
 * byte[] pdf = new AppointmentListPdfGenerator().generate(data);
 * </pre>
 */
public class AppointmentListPdfGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /**
     * Generuje raport jako tablicę bajtów gotową do wysłania przez HTTP.
     *
     * @param data dane raportu z backendu
     * @return bajty pliku PDF
     * @throws IOException gdy wystąpi błąd zapisu
     */
    public byte[] generate(AppointmentListReportData data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc)) {

            document.setMargins(40, 40, 40, 40);

            // Nagłówek
            document.add(PdfStyles.reportTitle("Raport: Lista wizyt"));
            document.add(PdfStyles.reportSubtitle(
                    data.clinicName() + "  |  " +
                            data.dateFrom().format(DATE_FMT) + " – " + data.dateTo().format(DATE_FMT)));

            // Parametry filtrowania
            if (data.doctorFilter() != null) {
                document.add(PdfStyles.infoLine("Lekarz", data.doctorFilter()));
            }
            if (data.locationFilter() != null) {
                document.add(PdfStyles.infoLine("Lokalizacja", data.locationFilter()));
            }
            if (data.statusFilter() != null) {
                document.add(PdfStyles.infoLine("Status", data.statusFilter()));
            }

            // Tabela wizyt
            document.add(PdfStyles.sectionTitle("Wykaz wizyt"));

            Table table = PdfStyles.createTable(
                    "Data / Godzina", "Pacjent", "Lekarz", "Usługa", "Status");

            List<AppointmentListReportData.AppointmentRow> rows = data.appointments();
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

            // Podsumowanie
            long cancelled = rows.stream()
                    .filter(r -> "CANCELLED".equalsIgnoreCase(r.status())).count();
            long noShow = rows.stream()
                    .filter(r -> "NO_SHOW".equalsIgnoreCase(r.status())).count();

            document.add(new Paragraph("\n"));
            document.add(PdfStyles.summaryLine("Podsumowanie"));
            document.add(PdfStyles.infoLine("Łączna liczba wizyt", String.valueOf(rows.size())));
            document.add(PdfStyles.infoLine("Anulowane", String.valueOf(cancelled)));
            document.add(PdfStyles.infoLine("No-show", String.valueOf(noShow)));
        }

        return baos.toByteArray();
    }
}
