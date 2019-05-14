package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.LargeElement;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class ChapterAndDynamicHeader {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/55524484/how-to-print-dynamic-string-in-pdf-header-using-onendpage-method">
     * How to print dynamic string in PDF header using onEndPage method
     * </a>
     * <p>
     * The problem is, as the OP has eventually found out himself, that
     * the content including all tables is not added to the document
     * before the <code>document.add(chapter)</code> right before closing
     * the document; thus, all changes to the event handler properties
     * have already occurred, the final table name being set.
     * </p>
     * <p>
     * To fix this one needs to add stuff more early, see
     * {@link #testLikeYuvarajChitelaImproved()}.
     * </p>
     */
    @Test
    public void testLikeYuvarajChitela() throws IOException, DocumentException {
        File file = new File(RESULT_FOLDER, "ChapterAndDynamicHeader.pdf");
        FileOutputStream fileout = new FileOutputStream(file);
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 55, 25);
        PdfWriter writer = PdfWriter.getInstance(document, fileout);
        ReportHeader event = new ReportHeader();
        writer.setPageEvent(event);
        writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);
        document.open();
        document.addAuthor("Me");
        document.addTitle("Table Report");

        Font font = FontFactory.getFont("TIMES_ROMAN", 12, BaseColor.BLACK);
        document.add(new Paragraph("Intro Page"));
        document.newPage();
        Chapter chapter = new Chapter(new Paragraph("Table \n\n"), 0);
        chapter.setNumberDepth(0);
        chapter.add(new Paragraph("   "));
        for (int i = 1; i < 5; i++) {

            float[] columnWidths = { 1f, 1f };
            // create PDF table with the given widths
            PdfPTable table = new PdfPTable(columnWidths);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.setWidthPercentage(30.0f);
            Section subsection = chapter.addSection(new Paragraph("Table "+i+" \n\n"), 0);
            event.setTableName("Table header" + i);
            writer.setPageEvent(event);
            table.addCell(new PdfPCell(new Phrase("Column 1", font)));
            table.addCell(new PdfPCell(new Phrase("Column 2", font)));
            table.setHeaderRows(1);
            for (int j = 0; j < 25; j++) {
                table.addCell(new PdfPCell(new Phrase("Hello" + j, font)));
                table.addCell(new PdfPCell(new Phrase("World" + j, font)));
            }
            subsection.add(table);
            subsection.newPage();

        }
        document.add(chapter);
        document.close();
        System.out.println("Done");
    }

    /**
     * <a href="https://stackoverflow.com/questions/55524484/how-to-print-dynamic-string-in-pdf-header-using-onendpage-method">
     * How to print dynamic string in PDF header using onEndPage method
     * </a>
     * <p>
     * The problem in {@link #testLikeYuvarajChitela()} is that the content
     * is added to the document as a single large chunk at the end when
     * all changes to the event handler properties have already occurred,
     * the final table name being set.
     * </p>
     * <p>
     * To fix this one needs to add stuff more early, e.g. by making use of
     * the fact that {@link Chapter} implements {@link LargeElement}.
     * </p>
     */
    @Test
    public void testLikeYuvarajChitelaImproved() throws IOException, DocumentException {
        File file = new File(RESULT_FOLDER, "ChapterAndDynamicHeader-improved.pdf");
        FileOutputStream fileout = new FileOutputStream(file);
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 55, 25);
        PdfWriter writer = PdfWriter.getInstance(document, fileout);
        ReportHeader event = new ReportHeader();
        writer.setPageEvent(event);
        writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);
        document.open();
        document.addAuthor("Me");
        document.addTitle("Table Report");

        Font font = FontFactory.getFont("TIMES_ROMAN", 12, BaseColor.BLACK);
        document.add(new Paragraph("Intro Page"));
        document.newPage();
        Chapter chapter = new Chapter(new Paragraph("Table \n\n"), 0);
        chapter.setComplete(false);
        chapter.setNumberDepth(0);
        chapter.add(new Paragraph("   "));
        for (int i = 1; i < 5; i++) {

            float[] columnWidths = { 1f, 1f };
            // create PDF table with the given widths
            PdfPTable table = new PdfPTable(columnWidths);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.setWidthPercentage(30.0f);
            document.add(chapter);
            Section subsection = chapter.addSection(new Paragraph("Table "+i+" \n\n"), 0);
            event.setTableName("Table header" + i);
            writer.setPageEvent(event);
            table.addCell(new PdfPCell(new Phrase("Column 1", font)));
            table.addCell(new PdfPCell(new Phrase("Column 2", font)));
            table.setHeaderRows(1);
            for (int j = 0; j < 25; j++) {
                table.addCell(new PdfPCell(new Phrase("Hello" + j, font)));
                table.addCell(new PdfPCell(new Phrase("World" + j, font)));
            }
            subsection.add(table);
            subsection.newPage();

        }
        chapter.setComplete(true);
        document.add(chapter);
        document.close();
        System.out.println("Done");
    }

    public class ReportHeader extends PdfPageEventHelper {

        private String tableName;

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable table2 = new PdfPTable(1);

            try {
                BaseColor basecolour = BaseColor.DARK_GRAY;
                Font fontboldHead = FontFactory.getFont("TIMES_ROMAN", 8, basecolour);

                table2.setTotalWidth(300);
                PdfPCell cell2 = new PdfPCell(new Paragraph(tableName, fontboldHead));
                cell2.setBorder(Rectangle.NO_BORDER);
                cell2.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell2.setVerticalAlignment(Element.ALIGN_BOTTOM);
                table2.addCell(cell2);
                table2.writeSelectedRows(0, -1, document.left(), 580, writer.getDirectContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
