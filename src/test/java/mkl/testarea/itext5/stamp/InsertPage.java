package mkl.testarea.itext5.stamp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * This test is for inserting pages using a {@link PdfStamper}.
 * 
 * @author mkl
 */
public class InsertPage
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "stamp");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/28911509/how-to-retain-page-labels-when-concatenating-an-existing-pdf-with-a-pdf-created">
     * How to retain page labels when concatenating an existing pdf with a pdf created from scratch?
     * </a>
     * <p>
     * A proposal how to implement the task using a {@link PdfStamper}.
     */
    @Test
    public void testInsertTitlePage() throws IOException, DocumentException
    {
        try (   InputStream documentStream = getClass().getResourceAsStream("template.pdf");
                InputStream titleStream = getClass().getResourceAsStream("title.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "test-with-title-page.pdf"))    )
        {
            PdfReader titleReader = new PdfReader(titleStream);
            PdfReader reader = new PdfReader(documentStream);
            PdfStamper stamper = new PdfStamper(reader, outputStream);

            PdfImportedPage page = stamper.getImportedPage(titleReader, 1);
            stamper.insertPage(1, titleReader.getPageSize(1));
            PdfContentByte content = stamper.getUnderContent(1);
            content.addTemplate(page, 0, 0);

            stamper.close();
        }
    }
}
