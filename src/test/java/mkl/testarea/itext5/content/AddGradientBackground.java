package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfShading;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class AddGradientBackground {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/52199010/itext-how-to-set-gradient-as-background-of-pdf-document">
     * IText - How To Set Gradient As Background Of PDF Document?
     * </a>
     * <p>
     * This test shows how to add a gradient background to an existing PDF.
     * </p>
     * @see #testCreateWithGradientBackground()
     */
    @Test
    public void testStampGradientBackground() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "test-with-gradient-background.pdf"))) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            pdfStamper.setRotateContents(false);
            for (int page = 1; page <= pdfReader.getNumberOfPages(); page++) {
                Rectangle pageSize = pdfReader.getPageSize(page);
                PdfShading axial = PdfShading.simpleAxial(pdfStamper.getWriter(),
                        pageSize.getLeft(pageSize.getWidth()/10), pageSize.getBottom(),
                        pageSize.getRight(pageSize.getWidth()/10), pageSize.getBottom(),
                        new BaseColor(255, 200, 200), new BaseColor(200, 255, 200), true, true);
                PdfContentByte canvas = pdfStamper.getUnderContent(page);
                canvas.paintShading(axial);
            }
            pdfStamper.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/52199010/itext-how-to-set-gradient-as-background-of-pdf-document">
     * IText - How To Set Gradient As Background Of PDF Document?
     * </a>
     * <p>
     * This test shows how to add a gradient background while creating a PDF from scratch.
     * </p>
     * @see #testCreateWithGradientBackground()
     * @see GradientBackground
     */
    @Test
    public void testCreateWithGradientBackground() throws IOException, DocumentException {
        try (   OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "created-with-gradient-background.pdf"))   ) {
            Document document = new Document();
            PdfWriter pdfWriter = PdfWriter.getInstance(document, result);
            pdfWriter.setPageEvent(new GradientBackground());
            document.open();
            for (int i=0; i < 20; i++) {
                document.add(new Paragraph("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."));
            }
            document.close();
        }
    }

    /**
     * @see AddGradientBackground#testCreateWithGradientBackground()
     */
    class GradientBackground extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle pageSize = document.getPageSize();
            PdfShading axial = PdfShading.simpleAxial(writer,
                    pageSize.getLeft(pageSize.getWidth()/10), pageSize.getBottom(),
                    pageSize.getRight(pageSize.getWidth()/10), pageSize.getBottom(),
                    new BaseColor(255, 200, 200), new BaseColor(200, 255, 200), true, true);
            PdfContentByte canvas = writer.getDirectContentUnder();
            canvas.paintShading(axial);
        }
    }
}
