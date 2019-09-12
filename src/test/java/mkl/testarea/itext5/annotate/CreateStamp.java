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

}
