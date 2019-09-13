package mkl.testarea.itext5.annotate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class CreateStamp {
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/253">
     * Issue with createStamp annotation on Adobe Acrobat Reader DC
     * </a>
     * <p>
     * Indeed, a stamp requires an appearance (otherwise Adobe
     * Reader attempts to create one). Furthermore, a name starting
     * with '#' should be used.
     * </p>
     */
    @Test
    public void testCreateLikeDprutean() throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter pdfWriter = PdfWriter.getInstance(document,
                new FileOutputStream(new File(RESULT_FOLDER, "StampLikeDprutean.pdf")));
        document.open();
        document.add(new Paragraph("This is a test document"));

        final PdfAnnotation annotation = PdfAnnotation.createStamp(pdfWriter, new Rectangle(300, 600, 400, 700), "Text",
                "#Comment");
        annotation.setAppearance(PdfName.N, pdfWriter.getDirectContent().createTemplate(100, 100));
        pdfWriter.addAnnotation(annotation);

        document.close();
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/253">
     * Issue with createStamp annotation on Adobe Acrobat Reader DC
     * </a>
     * <p>
     * In contrast to what the OP claims to have found, i.e.
     * that the size of the template changes the position of
     * the yellow balloon icon, the output of this test shows
     * no influence of the template size on that position.
     * </p>
     * @see #addStamp(PdfWriter, float, float, float, float, float, float)
     */
    @Test
    public void testCreateMultipleLikeDprutean() throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter pdfWriter = PdfWriter.getInstance(document,
                new FileOutputStream(new File(RESULT_FOLDER, "StampMultipleLikeDprutean.pdf")));
        document.open();
        document.add(new Paragraph("This is a test document"));

        addStamp(pdfWriter, 100, 600, 200, 700, 50, 50);
        addStamp(pdfWriter, 100, 400, 200, 500, 100, 100);
        addStamp(pdfWriter, 100, 200, 200, 300, 200, 200);

        addStamp(pdfWriter, 300, 600, 450, 750, 50, 50);
        addStamp(pdfWriter, 300, 400, 450, 550, 100, 100);
        addStamp(pdfWriter, 300, 200, 450, 350, 200, 200);

        document.close();
    }

    /** @see #testCreateMultipleLikeDprutean() */
    void addStamp(PdfWriter pdfWriter, float llx, float lly, float urx, float ury, float width, float height) {
        PdfAnnotation annotation = PdfAnnotation.createStamp(pdfWriter, new Rectangle(llx, lly, urx, ury),
                String.format("(%3.1f,%3.1f) to (%3.1f,%3.1f), template %3.1f\u00D7%3.1f", llx, lly, urx, ury, width, height),
                "#Comment");
        annotation.setAppearance(PdfName.N, pdfWriter.getDirectContent().createTemplate(width, height));
        pdfWriter.addAnnotation(annotation);

    }
}
