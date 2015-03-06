package mkl.testarea.itext5.stamp;

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

/**
 * This test is for testing basic stamping functionality
 * 
 * @author mkl
 */
public class Stamping
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "stamp");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href = "http://stackoverflow.com/questions/28898636/itextpdf-stop-transform-pdf-correctly">
     * Itextpdf stop transform pdf correctly
     * </a>
     * <p>
     * <a href="https://drive.google.com/file/d/0B3-DPMN-iMOmNjItRVJ4MHRZX3M/view?usp=sharing">
     * template.pdf
     * </a>
     */
    @Test
    public void testStampTemplate() throws DocumentException, IOException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("template.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "test.pdf"))    )
        {
            PdfReader reader = new PdfReader(resourceStream);
            PdfStamper stamper = new PdfStamper(reader, outputStream);

            stamper.close();
        }
    }

}
