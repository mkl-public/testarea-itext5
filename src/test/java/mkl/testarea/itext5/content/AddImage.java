package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.stream.StreamSupport;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.io.StreamUtil;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class AddImage {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/335">
     * Set specific page size and DPI
     * </a>
     * <p>
     * This test shows how to add an image to a document filling a whole
     * page with custom dimensions.
     * </p>
     */
    @Test
    public void testAddImageFillingPage() throws IOException, DocumentException {
        float width = 112.86f * 72f / 25.4f;
        float height = 169.33f * 72f / 25.4f;
        Rectangle rect = new Rectangle(width, height);
        Document doc = new Document(rect, 0, 0, 0, 0);
        PdfWriter.getInstance(doc, new FileOutputStream(new File(RESULT_FOLDER, "PageFillingImage.pdf")));
        doc.open();
        Image image;
        try (   InputStream imageResource = getClass().getResourceAsStream("/mkl/testarea/itext5/layer/Willi-1.jpg")) {
            image = Image.getInstance(StreamUtil.inputStreamToArray(imageResource));
        }
        image.scaleAbsolute(width, height);
        doc.add(image);
        doc.close();
    }

    /**
     * <a href="https://stackoverflow.com/questions/64065837/itext-black-image-in-pdf-created">
     * itext _ black image in pdf created
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1Qq_g7k-ngHFdHNHVeqJYuO_2dLtdH0hj/view?usp=sharing">
     * printer.jpeg
     * </a>
     * <p>
     * Cannot reproduce the issue, Adobe Acrobat Reader DC shows the image just fine.
     * </p>
     */
    @Test
    public void testAddImagePrinterLikeLuke() throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "PrinterImageLikeLuke.pdf")));
        document.open();

        byte[] byteArray = null;
        try (   InputStream imageResource = getClass().getResourceAsStream("printer.jpeg") ) {
            byteArray = StreamUtil.inputStreamToArray(imageResource);
        }
        Image image1 = Image.getInstance(byteArray);

        document.add(image1);
        document.close();
    }
}
