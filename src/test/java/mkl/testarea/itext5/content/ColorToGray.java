package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mklink
 *
 */
public class ColorToGray {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/58954234/itext-change-colour-of-existing-pdf-to-grayscale">
     * iText: Change Colour of existing PDF to Grayscale
     * </a>
     * <p>
     * This method shows how to grayscale the page contents of a
     * PDF by overlaying it with black in blend mode Saturation.
     * </p>
     * @see #testDropSaturationFACTSHEET05ENSG07100715FO02332302()
     * @see #testDropSaturationForklaringAvFakturan()
     * @see #testDropSaturationTest()
     * @see #testDropSaturationTransparency()
     */
    void dropSaturation(PdfStamper pdfStamper) {
        PdfGState gstate = new PdfGState();
        gstate.setBlendMode(PdfName.SATURATION);
        PdfReader pdfReader = pdfStamper.getReader();
        for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
            PdfContentByte canvas = pdfStamper.getOverContent(i);
            canvas.setGState(gstate);
            Rectangle mediaBox = pdfReader.getPageSize(i);
            canvas.setColorFill(BaseColor.BLACK);
            canvas.rectangle(mediaBox.getLeft(), mediaBox.getBottom(), mediaBox.getWidth(), mediaBox.getHeight());
            canvas.fill();
            canvas = pdfStamper.getUnderContent(i);
            canvas.setColorFill(BaseColor.WHITE);
            canvas.rectangle(mediaBox.getLeft(), mediaBox.getBottom(), mediaBox.getWidth(), mediaBox.getHeight());
            canvas.fill();
        }
    }

    /** @see #dropSaturation(PdfStamper) */
    @Test
    public void testDropSaturationFACTSHEET05ENSG07100715FO02332302() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/FACTSHEET05ENSG07100715FO02332302.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "FACTSHEET05ENSG07100715FO02332302-dropSaturation.pdf"))  ) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            dropSaturation(pdfStamper);
            pdfStamper.close();
        }
    }

    /** @see #dropSaturation(PdfStamper) */
    @Test
    public void testDropSaturationForklaringAvFakturan() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("Forklaring_av_fakturan.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "Forklaring_av_fakturan-dropSaturation.pdf"))  ) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            dropSaturation(pdfStamper);
            pdfStamper.close();
        }
    }

    /** @see #dropSaturation(PdfStamper) */
    @Test
    public void testDropSaturationTest() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("test.pdf ");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "test-dropSaturation.pdf"))  ) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            dropSaturation(pdfStamper);
            pdfStamper.close();
        }
    }

    /** @see #dropSaturation(PdfStamper) */
    @Test
    public void testDropSaturationTransparency() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("transparency.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "transparency-dropSaturation.pdf"))  ) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            dropSaturation(pdfStamper);
            pdfStamper.close();
        }
    }
}
