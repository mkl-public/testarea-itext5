package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.awt.geom.AffineTransform;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PatternColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPatternPainter;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class PatternColorFill {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/51256622/how-to-define-an-offset-for-a-patterncolor-fill-in-itext">
     * How to define an offset for a PatternColor fill in iText?
     * </a>
     * <p>
     * This is the OP's original code made runnable in iText 5.
     * </p>
     * @see #testFillLikeJTecOffset() 
     */
    @Test
    public void testFillLikeJTec() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("transparency.pdf");
                FileOutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "transparency-pattern-JTec.pdf"))) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(pdfReader, result);

            List<String> watermarkLines = getWatermarkLines();
            Rectangle watermarkRect = getWatermarkRect();

            PdfContentByte over = stamper.getOverContent(1);
            PdfPatternPainter painter = over.createPattern(watermarkRect.getWidth(), watermarkRect.getHeight());
            for (int x = 0; x < watermarkLines.size(); x++) {
              AffineTransform trans = getWatermarkTransform(watermarkLines, x);
              ColumnText.showTextAligned(painter, 0, new Phrase(watermarkLines.get(x)), (float) trans.getTranslateX(), (float) trans.getTranslateY(), 45f);
            }

            Rectangle pageSize = pdfReader.getPageSize(1);
            over.setColorFill(new PatternColor(painter));
            over.rectangle(0, 0, pageSize.getWidth(), pageSize.getHeight());
            over.fill();
            stamper.close();
            pdfReader.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/51256622/how-to-define-an-offset-for-a-patterncolor-fill-in-itext">
     * How to define an offset for a PatternColor fill in iText?
     * </a>
     * <p>
     * This is the OP's code enhanced to apply an offset that makes the
     * pattern start in the top left of the visible page.
     * </p>
     * @see #testFillLikeJTec()
     */
    @Test
    public void testFillLikeJTecOffset() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("transparency.pdf");
                FileOutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "transparency-pattern-Offset.pdf"))) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(pdfReader, result);

            List<String> watermarkLines = getWatermarkLines();
            Rectangle watermarkRect = getWatermarkRect();

            Rectangle pageSize = pdfReader.getCropBox(1);
            float xOff = pageSize.getLeft();
            float yOff = pageSize.getBottom() + ((int)pageSize.getHeight()) % ((int)watermarkRect.getHeight());

            PdfContentByte over = stamper.getOverContent(1);
            PdfPatternPainter painter = over.createPattern(2 * watermarkRect.getWidth(), 2 * watermarkRect.getHeight(), watermarkRect.getWidth(), watermarkRect.getHeight());
            for (int x = 0; x < watermarkLines.size(); x++) {
              AffineTransform trans = getWatermarkTransform(watermarkLines, x);
              ColumnText.showTextAligned(painter, 0, new Phrase(watermarkLines.get(x)), (float) trans.getTranslateX() + xOff, (float) trans.getTranslateY() + yOff, 45f);
            }

            over.setColorFill(new PatternColor(painter));
            over.rectangle(0, 0, pageSize.getWidth(), pageSize.getHeight());
            over.fill();
            stamper.close();
            pdfReader.close();
        }
    }

    static AffineTransform getWatermarkTransform(List<String> watermarkLines, int x) {
        return AffineTransform.getTranslateInstance(6 + 15*x, 6);
    }

    static Rectangle getWatermarkRect() {
        return new Rectangle(65, 50);
    }

    static List<String> getWatermarkLines() {
        return Arrays.asList("Test line 1", "Test line 2");
    }
}
