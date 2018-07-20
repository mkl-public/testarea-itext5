package mkl.testarea.itext5.meta;

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
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class SetPermissions {
    final static File RESULT_FOLDER = new File("target/test-outputs", "meta");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/51418933/pdfwriter-allow-assembly-does-not-change-the-value-for-document-assembly-in-th">
     * PdfWriter.ALLOW_ASSEMBLY does not change the value for “Document Assembly” in the PDF document
     * </a>
     * <p>
     * This tests creates a document with permissions like the OP.
     * Opening the document in Adobe Acrobat indeed shows that the
     * given permission is set.
     * </p>
     */
    @Test
    public void test() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                OutputStream out = new FileOutputStream(new File(RESULT_FOLDER, "test-permissions.pdf"))) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(pdfReader, out);

            stamper.setEncryption("password".getBytes(), "password1".getBytes(), PdfWriter.ALLOW_ASSEMBLY, PdfWriter.STANDARD_ENCRYPTION_128);
            
            stamper.close();
        }
    }

}
