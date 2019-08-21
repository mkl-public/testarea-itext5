package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class CreateFooter {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    Phrase total = null;

    /**
     * <a href="https://stackoverflow.com/questions/57577981/how-to-add-page-number-on-the-footer-of-pdf-using-itext-which-should-take-care-o">
     * How to add page number on the footer of pdf using itext which should take care of its width?
     * </a>
     * <p>
     * This test essentially contains the OP's original code.
     * Indeed, already for two-digit page numbers the left
     * cell, "Page x of", breaks into two lines. The cause
     * is that the left column is too small to host more than
     * single-digit page numbers.
     * </p>
     * @see #testCreateFooterLikeShashiShekharImproved()
     */
    @Test
    public void testCreateFooterLikeShashiShekhar() throws DocumentException, IOException {
        try (   OutputStream pdfStream = new FileOutputStream(new File(RESULT_FOLDER, "FooterLikeShashiShekhar.pdf")))
        {
            Document pdfDocument = new Document();
            PdfWriter pdfWriter = PdfWriter.getInstance(pdfDocument, pdfStream);
            pdfWriter.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    addFooter(writer);
                }
            });
            pdfDocument.open();

            int pageTotal = 100;
            total = new Phrase(String.valueOf(pageTotal), new Font(Font.FontFamily.HELVETICA, 8));

            for (int i = 0; i < pageTotal; i++) {
                pdfDocument.add(new Paragraph(String.format("Text on page %s.", i + 1)));
                pdfDocument.newPage();
            }

            pdfDocument.close();
        }
    }

    /** @see #testCreateFooterLikeShashiShekhar() */
    private void addFooter(PdfWriter writer){
        PdfPTable footer = new PdfPTable(2);
        try {
            // set defaults
            footer.setWidths(new int[]{2, 24});
            footer.setWidthPercentage(50);

            footer.setTotalWidth(527);
            footer.setLockedWidth(true);
            footer.getDefaultCell().setFixedHeight(30);
            footer.getDefaultCell().setBorder(Rectangle.TOP);
            footer.getDefaultCell().setBorderColor(BaseColor.RED);
            // here for the text Page 100 of, word of goes below in next line. 
            //It should be in same line.
            footer.addCell(new Phrase(String.format("Page %d of", Integer.parseInt(writer.getPageNumber()/*+"33"*/+"")), new Font(Font.FontFamily.HELVETICA, 8)));

            // add placeholder for total page count
            PdfPCell totalPageCount = new PdfPCell(total);
            totalPageCount.setBorder(Rectangle.TOP);
            totalPageCount.setBorderColor(BaseColor.GREEN);
            footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            footer.addCell(totalPageCount);

            // write page
            PdfContentByte canvas = writer.getDirectContent();
            canvas.beginMarkedContentSequence(PdfName.ARTIFACT);
            footer.writeSelectedRows(0, -1, 34, 20, canvas);
            canvas.endMarkedContentSequence();
        } catch(DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/57577981/how-to-add-page-number-on-the-footer-of-pdf-using-itext-which-should-take-care-o">
     * How to add page number on the footer of pdf using itext which should take care of its width?
     * </a>
     * <p>
     * This test improves the OP's original code a bit by
     * widening the first column of the footer table and
     * right-aligning it to prevent an unnatural gap between
     * "Page x of" and the total number of pages.
     * </p>
     * @see #testCreateFooterLikeShashiShekhar()
     */
    @Test
    public void testCreateFooterLikeShashiShekharImproved() throws DocumentException, IOException {
        try (   OutputStream pdfStream = new FileOutputStream(new File(RESULT_FOLDER, "FooterLikeShashiShekharImproved.pdf")))
        {
            Document pdfDocument = new Document();
            PdfWriter pdfWriter = PdfWriter.getInstance(pdfDocument, pdfStream);
            pdfWriter.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    addFooterImproved(writer);
                }
            });
            pdfDocument.open();

            int pageTotal = 100;
            total = new Phrase(String.valueOf(pageTotal), new Font(Font.FontFamily.HELVETICA, 8));

            for (int i = 0; i < pageTotal; i++) {
                pdfDocument.add(new Paragraph(String.format("Text on page %s.", i + 1)));
                pdfDocument.newPage();
            }

            pdfDocument.close();
        }
    }

    /** @see CreateFooter#testCreateFooterLikeShashiShekharImproved() */
    private void addFooterImproved(PdfWriter writer){
        PdfPTable footer = new PdfPTable(2);
        try {
            // set defaults
            footer.setWidths(new int[]{2, 20});
            footer.setWidthPercentage(50);

            footer.setTotalWidth(527);
            footer.setLockedWidth(true);
            footer.getDefaultCell().setFixedHeight(30);
            footer.getDefaultCell().setBorder(Rectangle.TOP);
            footer.getDefaultCell().setBorderColor(BaseColor.RED);
            footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            // here for the text Page 100 of, word of goes below in next line. 
            //It should be in same line.
            footer.addCell(new Phrase(String.format("Page %d of", Integer.parseInt(writer.getPageNumber()/*+"33"*/+"")), new Font(Font.FontFamily.HELVETICA, 8)));

            // add placeholder for total page count
            PdfPCell totalPageCount = new PdfPCell(total);
            totalPageCount.setBorder(Rectangle.TOP);
            totalPageCount.setBorderColor(BaseColor.GREEN);
            footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            footer.addCell(totalPageCount);

            // write page
            PdfContentByte canvas = writer.getDirectContent();
            canvas.beginMarkedContentSequence(PdfName.ARTIFACT);
            footer.writeSelectedRows(0, -1, 34, 20, canvas);
            canvas.endMarkedContentSequence();
        } catch(DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }
}
