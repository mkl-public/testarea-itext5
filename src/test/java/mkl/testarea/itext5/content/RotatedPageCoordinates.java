package mkl.testarea.itext5.content;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class RotatedPageCoordinates {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/56965809/after-resize-document-new-content-have-as-same-size-how-save-new-size-in-bytea">
     * After resize document, new content have as same size. How save new size in ByteArrayOutputStream?
     * </a>
     * <p>
     * The problem at hand is that the OP uses the reader method
     * <code>getPageSize</code>. In his case, though, he should
     * use <code>getPageSizeWithRotation</code>.
     * </p>
     */
    @Test
    public void testLikeMflorczak() throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 0, 0, 0, 0);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        document.open();
        writer.setPageEmpty(false);
        document.close();

        byte[] emptyBytes = outputStream.toByteArray();
        outputStream.reset();
        Files.write(new File(RESULT_FOLDER, "Mflorczak empty.pdf").toPath(), emptyBytes);

        PdfReader reader = new PdfReader(emptyBytes);
        int n = reader.getNumberOfPages();
        PdfStamper stamper = new PdfStamper(reader,outputStream);
        PdfContentByte pageContent;
        for (int i = 0; i < n;) {
            pageContent = stamper.getOverContent(++i);
            System.out.println(reader.getPageSize(i));
            ColumnText.showTextAligned(pageContent, Element.ALIGN_RIGHT,
                    new Phrase(String.format("page %s of %s", i, n)), 
                        reader.getPageSize(i).getWidth()- 20, 20, 0);
        }
        stamper.close();

        byte[] stampedBytes = outputStream.toByteArray();
        outputStream.reset();
        Files.write(new File(RESULT_FOLDER, "Mflorczak stamped.pdf").toPath(), stampedBytes);

        reader = new PdfReader(emptyBytes);
        n = reader.getNumberOfPages();
        stamper = new PdfStamper(reader,outputStream);
        for (int i = 0; i < n;) {
            pageContent = stamper.getOverContent(++i);
            System.out.println(reader.getPageSizeWithRotation(i));
            ColumnText.showTextAligned(pageContent, Element.ALIGN_RIGHT,
                    new Phrase(String.format("page %s of %s", i, n)), 
                        reader.getPageSizeWithRotation(i).getWidth()- 20, 20, 0);
        }
        stamper.close();

        stampedBytes = outputStream.toByteArray();
        outputStream.reset();
        Files.write(new File(RESULT_FOLDER, "Mflorczak stamped-improved.pdf").toPath(), stampedBytes);
    }

}
