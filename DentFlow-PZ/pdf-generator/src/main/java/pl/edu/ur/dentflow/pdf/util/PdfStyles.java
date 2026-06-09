package pl.edu.ur.dentflow.pdf.util;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.IOException;

/**
 * Shared styles and components for all DentFlow PDF reports.
 */
public final class PdfStyles {

    // DentFlow colors
    public static final DeviceRgb COLOR_PRIMARY = new DeviceRgb(0, 90, 160); // dark blue
    public static final DeviceRgb COLOR_SECONDARY = new DeviceRgb(240, 247, 255); // light header background
    public static final DeviceRgb COLOR_TABLE_HEAD = new DeviceRgb(0, 90, 160);
    public static final DeviceRgb COLOR_TABLE_ROW_ALT = new DeviceRgb(245, 249, 255);

    private PdfStyles() {
    }

    public static PdfFont fontBold() {
        try {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load bold font", e);
        }
    }

    public static PdfFont fontRegular() {
        try {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load regular font", e);
        }
    }

    /**
     * Report title: title + subtitle (e.g. date range, clinic).
     */
    public static Paragraph reportTitle(String title) {
        return new Paragraph(title)
                .setFont(fontBold())
                .setFontSize(18)
                .setFontColor(COLOR_PRIMARY)
                .setMarginBottom(4);
    }

    public static Paragraph reportSubtitle(String subtitle) {
        return new Paragraph(subtitle)
                .setFont(fontRegular())
                .setFontSize(11)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginBottom(16);
    }

    public static Paragraph sectionTitle(String text) {
        return new Paragraph(text)
                .setFont(fontBold())
                .setFontSize(13)
                .setFontColor(COLOR_PRIMARY)
                .setMarginTop(14)
                .setMarginBottom(6);
    }

    public static Paragraph infoLine(String label, String value) {
        return new Paragraph(label + ": " + value)
                .setFont(fontRegular())
                .setFontSize(10)
                .setMarginBottom(2);
    }

    public static Paragraph summaryLine(String text) {
        return new Paragraph(text)
                .setFont(fontBold())
                .setFontSize(10)
                .setFontColor(COLOR_PRIMARY)
                .setMarginTop(4);
    }

    /**
     * Creates a full-page-width table with given headers.
     */
    public static Table createTable(String... headers) {
        float[] widths = new float[headers.length];
        for (int i = 0; i < headers.length; i++)
            widths[i] = 1f;

        Table table = new Table(UnitValue.createPercentArray(widths))
                .useAllAvailableWidth()
                .setMarginTop(4);

        for (String header : headers) {
            table.addHeaderCell(
                    new Cell().add(new Paragraph(header)
                            .setFont(fontBold())
                            .setFontSize(10)
                            .setFontColor(ColorConstants.WHITE))
                            .setBackgroundColor(COLOR_TABLE_HEAD)
                            .setPadding(6)
                            .setTextAlignment(TextAlignment.CENTER));
        }
        return table;
    }

    /**
     * Adds a row to the table with automatic alternating row coloring.
     */
    public static void addRow(Table table, int rowIndex, String... values) {
        com.itextpdf.kernel.colors.Color bg = (rowIndex % 2 == 0)
                ? ColorConstants.WHITE
                : COLOR_TABLE_ROW_ALT;
        for (String value : values) {
            table.addCell(
                    new Cell().add(new Paragraph(value == null ? "" : value)
                            .setFont(fontRegular())
                            .setFontSize(9))
                            .setBackgroundColor(bg)
                            .setPadding(5));
        }
    }
}
