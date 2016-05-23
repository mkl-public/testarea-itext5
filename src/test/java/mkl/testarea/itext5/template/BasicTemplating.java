// $Id$
package mkl.testarea.itext5.template;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfStamperHelper;

/**
 * This class tests the basic named page templating functionality
 * provided by the {@link PdfStamperHelper}.
 * 
 * @author mkl
 */
public class BasicTemplating
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "template");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * This test names a page of the source file.
     */
    @Test
    public void testNameTest() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                OutputStream target = new FileOutputStream(new File(RESULT_FOLDER, "test-named.pdf")) )
        {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, target, '\0', true);
            PdfStamperHelper.createTemplate(pdfStamper, "template", 1);
            pdfStamper.close();
        }
    }

    /**
     * This test names a page of the source file and spawns a new page from this template.
     */
    @Test
    public void testNameSpawnTest() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                OutputStream target = new FileOutputStream(new File(RESULT_FOLDER, "test-named-spawned.pdf")) )
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, baos, '\0', true);
            PdfStamperHelper.createTemplate(pdfStamper, "template", 1);
            pdfStamper.close();

            pdfReader = new PdfReader(baos.toByteArray());
            pdfStamper = new PdfStamper(pdfReader, target, '\0', true);
            PdfStamperHelper.spawnTemplate(pdfStamper, "template", 1);
            pdfStamper.close();
        }
    }

    /**
     * <p>
     * This test spawns a new page from a signed PDF; the template page had
     * been named before signing.
     * </p>
     * <p>
     * Unfortunately Adobe Acrobat Reader meanwhile considers signatures broken
     * after page templates have been spawned even though this change should be
     * allowed according to the PDF specification.
     * </p>
     */
    @Test
    public void testSpawnPdfaNamedSigned() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("pdfa-named-signed.pdf");
                OutputStream target = new FileOutputStream(new File(RESULT_FOLDER, "pdfa-named-signed-spawned.pdf")) )
        {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, target, '\0', true);
            PdfStamperHelper.spawnTemplate(pdfStamper, "myTemplate0", 3);
            pdfStamper.close();
        }
    }
}
