package mkl.testarea.itext5.stamp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class StampTextOnTagged {
    final static File RESULT_FOLDER = new File("target/test-outputs", "stamp");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/76388538/cannot-write-a-paragraph-to-a-pdf-file-using-itext-pdf">
     * Cannot write a paragraph to a pdf file using iText pdf
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1d2PKbf1pqXwZTboLZOJr69KJTJtAKYUx/view">
     * test_file.pdf
     * </a>
     * <p>
     * This is essentially the OP's code. Running it with iText 5.5.4 one indeed runs
     * into a <code>NullPointerException</code>. Running it with iText 5.5.13.3, though,
     * the code executes without issue.
     * </p>
     * <p>
     * Comparing the exception location code with its current form one finds that there
     * indeed was an unconditional call of a method of a <code>parent</code> object but
     * that now there is a call to a helper method that first checks the <code>parent</code>
     * and only calls its method if it isn't <code>null</code>.
     * </p>
     * <p>
     * The fix was committed by Alexander Chingarev on 2015-01-19 14:39:23 with
     * the message "Fixed NPE when modifying content of TaggedPDF document."
     * (e0eec31b31a414ad4fe70eda1aeea04fc25dd319)
     * </p>
     */
    @Test
    public void testLikeFrançoisVinckel() throws DocumentException, IOException {
        try (   InputStream resource = getClass().getResourceAsStream("test_file.pdf"); ){
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, "test_file-stamped.pdf")));

            PdfContentByte cb = stamper.getOverContent(1);
            ColumnText ct = new ColumnText(cb);
            ct.setSimpleColumn(new Rectangle(36, 600, 200, 800));
            ct.addElement(new Paragraph("I want to add this text in a rectangle defined by the coordinates llx = 36, lly = 600, urx = 200, ury = 800"));
            int status = ct.go();

            stamper.close();
            reader.close();
        }
    }

}
