package com.dentflow.pdf.generator;

import com.dentflow.pdf.model.RoomOccupancyReportData;
import com.dentflow.pdf.util.PdfStyles;
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
 * Generator PDF dla Raportu 2: Obłożenie gabinetu.
 */
public class RoomOccupancyPdfGenerator {

        private static final DecimalFormat DF = new DecimalFormat("0.0");

        /**
         * Generuje raport jako tablicę bajtów.
         *
         * @param data dane raportu z backendu
         * @return bajty pliku PDF
         * @throws IOException gdy wystąpi błąd zapisu
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

                        // Nagłówek
                        document.add(PdfStyles.reportTitle("Raport: Obłożenie gabinetu"));
                        document.add(PdfStyles.reportSubtitle(
                                        data.clinicName() + "  |  " + period +
                                                        (data.locationFilter() != null ? "  |  " + data.locationFilter()
                                                                        : "")));

                        // Wykres słupkowy (tekstowy)
                        document.add(PdfStyles.sectionTitle("Liczba wizyt dziennie"));

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

                        // Tabela lekarzy
                        document.add(PdfStyles.sectionTitle("Statystyki personelu"));

                        Table table = PdfStyles.createTable(
                                        "Lekarz", "Liczba wizyt", "Godziny pracy", "% Slotów");

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

                        // Statystyki ogólne
                        document.add(PdfStyles.sectionTitle("Statystyki ogólne"));
                        document.add(PdfStyles.infoLine("Średni czas wizyty",
                                        DF.format(data.avgAppointmentMinutes()) + " min"));
                        document.add(PdfStyles.infoLine("Wskaźnik no-show",
                                        DF.format(data.noShowRate()) + "%"));

                        if (data.topServices() != null && !data.topServices().isEmpty()) {
                                document.add(PdfStyles.infoLine("Najpopularniejsze usługi",
                                                String.join(", ", data.topServices())));
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
