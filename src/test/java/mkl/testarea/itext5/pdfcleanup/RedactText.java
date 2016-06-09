package mkl.testarea.itext5.pdfcleanup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpProcessor;

/**
 * @author mklink
 *
 */
public class RedactText
{
    final static File OUTPUTDIR = new File("target/test-outputs/pdfcleanup");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        OUTPUTDIR.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/35374912/itext-cleaning-up-text-in-rectangle-without-cleaning-full-row">
     * iText - Cleaning Up Text in Rectangle without cleaning full row
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/3i7g4w85dvul6db/input.pdf?dl=0">
     * input.pdf
     * </a>
     * <p>
     * Cannot reproduce the OP's issue.
     * </p>
     */
    @Test
    public void testRedactJavishsInput() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("input.pdf");
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "input-redactedJavish.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);

            List<Float> linkBounds = new ArrayList<Float>();
            linkBounds.add(0, (float) 200.7);
            linkBounds.add(1, (float) 547.3);
            linkBounds.add(2, (float) 263.3);
            linkBounds.add(3, (float) 558.4);

            Rectangle linkLocation1 = new Rectangle(linkBounds.get(0), linkBounds.get(1), linkBounds.get(2), linkBounds.get(3));
            List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
            cleanUpLocations.add(new PdfCleanUpLocation(1, linkLocation1, BaseColor.GRAY));

            PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations, stamper);
            cleaner.cleanUp();

            stamper.close();
            reader.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/35374912/itext-cleaning-up-text-in-rectangle-without-cleaning-full-row">
     * iText - Cleaning Up Text in Rectangle without cleaning full row
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/zeljo2k8tly7yqi/Test1.pdf?dl=0">
     * Test1.pdf
     * </a>
     * <p>
     * With this new file the issue could be reproduced, indeed an iText issue.
     * </p>
     */
    @Test
    public void testRedactJavishsTest1() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Test1.pdf");
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "Test1-redactedJavish.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);

            List<Float> linkBounds = new ArrayList<Float>();
            linkBounds.add(0, (float) 202.3);
            linkBounds.add(1, (float) 588.6);
            linkBounds.add(2, (float) 265.8);
            linkBounds.add(3, (float) 599.7);

            Rectangle linkLocation1 = new Rectangle(linkBounds.get(0), linkBounds.get(1), linkBounds.get(2), linkBounds.get(3));
            List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
            cleanUpLocations.add(new PdfCleanUpLocation(1, linkLocation1, BaseColor.GRAY));

            PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations, stamper);
            cleaner.cleanUp();

            stamper.close();
            reader.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/37713112/attempt-to-apply-redactions-results-in-exception">
     * Attempt to apply redactions results in exception
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0Bz0Wye-k7GsZUXdKbWtMTUNpcXc/view?usp=sharing">
     * 20150325101924-2102000595-SessionReport.pdf
     * </a>
     * <p>
     * This corresponds to the iTextSharp/C# code by the OP having fixed a first issue, the
     * use of annots added in the same PdfStamper sesion.
     * </p>
     * <p>
     * In Java no more exception occurs but in C# one does. This is due to iText using a HashMap
     * and iTextSharp using a Dictionary as a member of the PdfCleanUpProcessor to which
     * annotation rectangles are added by their annotations index in their respective page
     * annotations array (which is a logical error on both sides). HashMaps allow overwriting
     * entries, Dictionaries don't. Thus, iTextSharp hickups and iText does not.
     * </p>
     */
    @Test
    public void testRedact20150325101924_2102000595_SessionReport() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("20150325101924-2102000595-SessionReport.pdf");
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "20150325101924-2102000595-SessionReport-annotated.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);

            PdfAnnotation pdfAnot1 = new PdfAnnotation(stamper.getWriter(), new Rectangle(165f, 685f, 320f, 702f));
            pdfAnot1.setTitle("First Page");
            pdfAnot1.put(PdfName.SUBTYPE, PdfName.REDACT);
            pdfAnot1.put(PdfName.IC, new PdfArray(new float[] { 0f, 0f, 0f }));
            pdfAnot1.put(PdfName.OC, new PdfArray(new float[] { 1f, 0f, 0f })); // red outline
            pdfAnot1.put(PdfName.QUADPOINTS, new PdfArray());
            stamper.addAnnotation(pdfAnot1, 1);
            for (int i = 1; i <= reader.getNumberOfPages(); i++)
            {
                PdfAnnotation pdfAnot2 = new PdfAnnotation(stamper.getWriter(), new Rectangle(220f, 752f, 420f, 768f));
                pdfAnot2.setTitle("Header");
                pdfAnot2.put(PdfName.SUBTYPE, PdfName.REDACT);
                pdfAnot2.put(PdfName.IC, new PdfArray(new float[] { 0f, 0f, 0f }));
                pdfAnot2.put(PdfName.OC, new PdfArray(new float[] { 1f, 0f, 0f })); // red outline
                pdfAnot2.put(PdfName.QUADPOINTS, new PdfArray());
                stamper.addAnnotation(pdfAnot2, i);
            }

            stamper.close();
        }

        try (   InputStream resource = new FileInputStream(new File(OUTPUTDIR, "20150325101924-2102000595-SessionReport-annotated.pdf"));
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "20150325101924-2102000595-SessionReport-redacted.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);

            PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(stamper);
            cleaner.cleanUp();
            
            stamper.close();
        }
    }
}
