package mkl.testarea.itext5.pdfcleanup;

import java.io.File;
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
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
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
}
