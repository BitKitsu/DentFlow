package com.dentflow.pdf.generator;

import com.dentflow.pdf.model.PatientVisitHistoryReportData;
import com.dentflow.pdf.util.PdfStyles;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Generator PDF dla Raportu 3: Historia wizyt pacjenta.
 */
public class PatientVisitHistoryPdfGenerator {

    /**
     * Generuje raport jako tablicę bajtów.
     *
     * @param data dane raportu z backendu
     * @return bajty pliku PDF
     * @throws IOException gdy wystąpi błąd zapisu
     */
    public byte[] generate(PatientVisitHistoryReportData data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc)) {

            document.setMargins(40, 40, 40, 40);

            // Nagłówek
            document.add(PdfStyles.reportTitle("Raport: Historia wizyt pacjenta"));
            document.add(PdfStyles.reportSubtitle(
                    data.clinicName() + "  |  Zakres dat: " + data.dateRangeDescription()));

            // Dane pacjenta
            document.add(PdfStyles.sectionTitle("Dane pacjenta"));
            document.add(PdfStyles.infoLine("Imię i nazwisko",
                    data.patientFirstName() + " " + data.patientLastName()));
            if (data.patientPhone() != null && !data.patientPhone().isBlank()) {
                document.add(PdfStyles.infoLine("Telefon", data.patientPhone()));
            }
            if (data.patientEmail() != null && !data.patientEmail().isBlank()) {
                document.add(PdfStyles.infoLine("E-mail", data.patientEmail()));
            }

            // Tabela wizyt
            document.add(PdfStyles.sectionTitle("Historia wizyt"));

            Table table = PdfStyles.createTable(
                    "Data", "Lekarz", "Usługa", "Status", "Notatki");

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

            // Podsumowanie
            document.add(new Paragraph("\n"));
            document.add(PdfStyles.summaryLine("Podsumowanie"));
            document.add(PdfStyles.infoLine("Łączna liczba wizyt", String.valueOf(visits.size())));

            visits.stream()
                    .map(PatientVisitHistoryReportData.VisitRow::date)
                    .max(String::compareTo)
                    .ifPresent(lastDate -> document.add(PdfStyles.infoLine("Ostatnia wizyta", lastDate)));
        }

        return baos.toByteArray();
    }
}
