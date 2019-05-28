package mkl.testarea.itext5.pdfcleanup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.io.StreamUtil;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpProcessor;

/**
 * @author mkl
 */
public class RedactWithImageIssue {
    final static File OUTPUTDIR = new File("target/test-outputs/pdfcleanup");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        OUTPUTDIR.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/56186498/what-makes-pdfstamper-to-remove-images-from-pdf-after-cleanup-though-it-should">
     * What makes PdfStamper to remove images from pdf after cleanup() though it shouldn't?
     * </a>
     * <br/>
     * <a href="https://drive.google.com/drive/folders/1buvmQCiHhrbVD_i0mJhrdCo3YM9ngL3p?usp=sharing">
     * bad.pdf
     * </a>
     * <p>
     * Indeed, when redacting this "bad" PDF the image vanishes without it being
     * in the redaction areas.
     * </p>
     */
    @Test
    public void testRedactBadLikeShatterwoods() throws IOException, DocumentException {
        PdfReader reader = new PdfReader(getClass().getResourceAsStream("bad.pdf"));
        int i = 1;
        File outFile = new File(OUTPUTDIR, "bad-redacted-like-shatterwoods.pdf");
        String OWNER_PASSWORD = "owner";

        //...
        final Rectangle RECT_TOP= new Rectangle(25f, 788f, 288f, 812.5f);
        final Rectangle RECT_BOT= new Rectangle(103.5f, 36.5f, 331f, 40f);
        //...

        Document document = new Document(reader.getPageSizeWithRotation(1));
        File tempFile = File.createTempFile("temp", ".pdf");
        PdfCopy writer = new PdfCopy(
                document, //PdfDocument
                new FileOutputStream(tempFile.getAbsolutePath()));

       document.open(); 

        writer.addPage( writer.getImportedPage(reader, i) );
        writer.addPage( writer.getImportedPage(reader, i + 1) );

        writer.freeReader(reader);
        writer.close();
        document.close(); 

        PdfReader tmpReader = new PdfReader(tempFile.getAbsolutePath());
        PdfStamper st = new PdfStamper(tmpReader, new FileOutputStream(outFile));
        List<com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation> locations = new ArrayList<>();
        locations.add(new com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation(1, RECT_TOP, BaseColor.WHITE));
        locations.add(new com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation(1, RECT_BOT, BaseColor.WHITE));

        new PdfCleanUpProcessor(locations, st).cleanUp();
        st.setEncryption(
            "".getBytes(),
            OWNER_PASSWORD.getBytes(),
            PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_PRINTING,
            PdfWriter.ENCRYPTION_AES_256 | PdfWriter.DO_NOT_ENCRYPT_METADATA);

        st.getWriter().freeReader(tmpReader);
        st.close();
        tmpReader.close();
        tempFile.delete();
    }

    /**
     * <a href="https://stackoverflow.com/questions/56186498/what-makes-pdfstamper-to-remove-images-from-pdf-after-cleanup-though-it-should">
     * What makes PdfStamper to remove images from pdf after cleanup() though it shouldn't?
     * </a>
     * <br/>
     * <a href="https://drive.google.com/drive/folders/1buvmQCiHhrbVD_i0mJhrdCo3YM9ngL3p?usp=sharing">
     * good.pdf
     * </a>
     * <p>
     * Indeed, when redacting this "good" PDF the image does not vanishes.
     * </p>
     */
    @Test
    public void testRedactGoodLikeShatterwoods() throws IOException, DocumentException {
        PdfReader reader = new PdfReader(getClass().getResourceAsStream("good.pdf"));
        int i = 1;
        File outFile = new File(OUTPUTDIR, "good-redacted-like-shatterwoods.pdf");
        String OWNER_PASSWORD = "owner";

        //...
        final Rectangle RECT_TOP= new Rectangle(25f, 788f, 288f, 812.5f);
        final Rectangle RECT_BOT= new Rectangle(103.5f, 36.5f, 331f, 40f);
        //...

        Document document = new Document(reader.getPageSizeWithRotation(1));
        File tempFile = File.createTempFile("temp", ".pdf");
        PdfCopy writer = new PdfCopy(
                document, //PdfDocument
                new FileOutputStream(tempFile.getAbsolutePath()));

       document.open(); 

        writer.addPage( writer.getImportedPage(reader, i) );
        writer.addPage( writer.getImportedPage(reader, i + 1) );

        writer.freeReader(reader);
        writer.close();
        document.close(); 

        PdfReader tmpReader = new PdfReader(tempFile.getAbsolutePath());
        PdfStamper st = new PdfStamper(tmpReader, new FileOutputStream(outFile));
        List<com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation> locations = new ArrayList<>();
        locations.add(new com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation(1, RECT_TOP, BaseColor.WHITE));
        locations.add(new com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation(1, RECT_BOT, BaseColor.WHITE));

        new PdfCleanUpProcessor(locations, st).cleanUp();
        st.setEncryption(
            "".getBytes(),
            OWNER_PASSWORD.getBytes(),  
            PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_PRINTING,
            PdfWriter.ENCRYPTION_AES_256 | PdfWriter.DO_NOT_ENCRYPT_METADATA);

        st.getWriter().freeReader(tmpReader);
        st.close();
        tmpReader.close();
        tempFile.delete();
    }

    /**
     * <a href="https://stackoverflow.com/questions/56186498/what-makes-pdfstamper-to-remove-images-from-pdf-after-cleanup-though-it-should">
     * What makes PdfStamper to remove images from pdf after cleanup() though it shouldn't?
     * </a>
     * <p>
     * As it turned out, in the "bad" test PDF of the OP the
     * unexpectedly vanishing image is an inline image completely
     * outside all redaction areas. This test illustrates the
     * issue even more clearly using a PDF containing only 5
     * inline images. iText redaction applied with the readaction
     * area covering the third image completely, the second and
     * fourth partially, and the first and fifth not at all,
     * results in the first and fifth image vanishing.
     * </p>
     */
    @Test
    public void testRedactPdfWithInlineImages() throws IOException, DocumentException {
        byte[] pdf = createPdfWithInlineImages();
        Files.write(new File(OUTPUTDIR, "pdfWithInlineImages.pdf").toPath(), pdf);

        PdfReader reader = new PdfReader(pdf);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(OUTPUTDIR, "pdfWithInlineImages-redacted.pdf")));
        List<com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation> locations = new ArrayList<>();
        locations.add(new com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation(1, new Rectangle(150, 150, 350, 350), BaseColor.RED));
        new PdfCleanUpProcessor(locations, stamper).cleanUp();
        stamper.close();
    }

    /** @see #testRedactPdfWithInlineImages() */
    byte[] createPdfWithInlineImages() throws DocumentException, IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Image image;
        try (   InputStream imageResource = getClass().getResourceAsStream("/mkl/testarea/itext5/content/2x2colored.png")) {
            image = Image.getInstance(StreamUtil.inputStreamToArray(imageResource));
        }

        Document document = new Document(new Rectangle(500, 500));
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        PdfContentByte canvas = writer.getDirectContent();
        for (int i = 0; i < 5; i++) {
            canvas.addImage(image, 50, 0, 0, 50, i * 100 + 25, i * 100 + 25, true);
        }
        document.close();

        return baos.toByteArray();
    }
}
