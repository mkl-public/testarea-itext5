package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class AddSpecialImage {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/52473246/itextpdf-throws-illegalargumentexception-while-converting-tiled-tiff-into-pdf">
     * ItextPdf throws IllegalArgumentException while converting tiled tiff into pdf
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1z2rJGBZLlIFlxyR-fwBs5kMeHOth3ADH">
     * 02-Scan_20180813_11371847_128.tif
     * </a>
     * <p>
     * Indeed, this throws an {@link IllegalArgumentException}, and if you look at its
     * message, you'll read "Tiles are not supported."
     * </p>
     */
    @Test
    public void testAdd02Scan_20180813_11371847_128_tif() throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "With02-Scan_20180813_11371847_128_tif.pdf")));
        document.open();

        Image image = null;
        try (InputStream imageStream = getClass().getResourceAsStream("02-Scan_20180813_11371847_128.tif"))
        {
            image = Image.getInstance(IOUtils.toByteArray(imageStream));
            image.scaleToFit(110,110);
        }

        document.add(image);

        document.close();
    }

}
