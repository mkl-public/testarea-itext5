package mkl.testarea.itext5.signature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.io.StreamUtil;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfAppearance;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class AnnotateSignedPdf {
    final static File RESULT_FOLDER = new File("target/test-outputs", "signature");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/71005453/at-least-one-signature-is-invalid-after-adding-stamp-in-signed-pdf">
     * "At least one signature is invalid" After adding stamp in Signed PDF
     * </a>
     * <p>
     * This test illustrates how to add a stamp annotation with an image
     * to a signed PDF without invalidating the signature. In contrast to
     * the OP's code we don't use the OverContent of a page to create a
     * {@link PdfAppearance} instance but instead use the static method
     * {@link PdfAppearance#createAppearance(com.itextpdf.text.pdf.PdfWriter, float, float)}.
     * Thus, we don't trigger the side effects of the OverContent retrieval.
     * </p>
     */
    @Test
    public void testAddImageStamp() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("BLANK-signed.pdf");
                InputStream image = getClass().getResourceAsStream("/mkl/testarea/itext5/layer/Willi-1.jpg");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "BLANK-signed-image.pdf"))   )
        {
            Image img = Image.getInstance(StreamUtil.inputStreamToArray(image));

            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result, '\0', true);

            float w = 100;
            float h = 100;
            Rectangle location = new Rectangle(36, 770 - h, 36 + w, 770);

            PdfAnnotation stamp = PdfAnnotation.createStamp(pdfStamper.getWriter(), location, null, "ITEXT");
            img.setAbsolutePosition(0, 0);
            PdfAppearance app = PdfAppearance.createAppearance(pdfStamper.getWriter(), 100, 100);
            app.addImage(img);
            stamp.setAppearance(PdfName.N, app);
            pdfStamper.addAnnotation(stamp, 1);

            pdfStamper.close();
        }
    }

}
