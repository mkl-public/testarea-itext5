/**
 * 
 */
package mkl.testarea.itext5.annotate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfFileSpecification;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class AddMultipleFileAttachments {
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/77988848/itext-failing-to-attach-files-when-adding-more-than-60-attachments">
     * itext failing to attach files when adding more than 60 attachments
     * </a>
     * <p>
     * This test attaches 73 file attachments to a PDF, works as expected, so
     * cannot reproduce the issue of the OP.
     * </p>
     */
    @Test
    public void testAdd73Files() throws DocumentException, IOException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "73Attachments.pdf")));
        document.open();

        document.add(new Paragraph("Test file with 73 attachments"));

        for (int i = 0; i < 73; i++) {
            PdfFileSpecification fs = PdfFileSpecification.fileEmbedded(writer,
                    String.format("folder/file_%d.txt", i),
                    String.format("File %d.txt", i),
                    String.format("Contents of file %d", i).getBytes());
            fs.addDescription("specificname", false);
            writer.addFileAttachment(fs);
         }

        document.close();
    }
}
