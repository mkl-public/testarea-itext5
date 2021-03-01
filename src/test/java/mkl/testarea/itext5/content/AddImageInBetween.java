package mkl.testarea.itext5.content;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Jpeg;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.io.StreamUtil;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class AddImageInBetween {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/66352174/background-image-itextpdf-5-5">
     * Background image itextpdf 5.5
     * </a>
     * <p>
     * This test applies the {@link UnderContentRemover} to test data
     * generated using the code provided by the OP and uses it to add
     * a stamp between the former UnderContent and DirectContent.
     * </p>
     */
    @Test
    public void testForVladimirSafonov() throws DocumentException, IOException {
        byte[] source = createPdfLikeVladimirSafonov();
        Files.write(new File(RESULT_FOLDER, "PdfLikeVladimirSafonov.pdf").toPath(), source);

        PdfReader pdfReader = new PdfReader(source);
        List<List<List<PdfObject>>> underContentByPage = new ArrayList<>();
        byte[] sourceWithoutUnderContent = null;
        try (   ByteArrayOutputStream outputStream = new ByteArrayOutputStream()    ) {
            PdfStamper pdfStamper = new PdfStamper(pdfReader, outputStream);

            UnderContentRemover underContentRemover = new UnderContentRemover();
            for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
                underContentRemover.clear();
                underContentRemover.editPage(pdfStamper, i);
                underContentByPage.add(underContentRemover.getUnderContent());
            }

            pdfStamper.close();
            pdfReader.close();

            sourceWithoutUnderContent = outputStream.toByteArray();
        }
        Files.write(new File(RESULT_FOLDER, "PdfLikeVladimirSafonov-WithoutUnderContent.pdf").toPath(), sourceWithoutUnderContent);

        Image background;
        try (   InputStream imageResource = getClass().getResourceAsStream("2x2colored.png")) {
            background = Image.getInstance(StreamUtil.inputStreamToArray(imageResource));
            background.scaleToFit(463F, 132F);
            background.setAbsolutePosition(275F, 100F);
        }
        pdfReader = new PdfReader(sourceWithoutUnderContent);
        byte[] sourceWithStampInbetween = null;
        try (   ByteArrayOutputStream outputStream = new ByteArrayOutputStream()    ) {
            PdfStamper pdfStamper = new PdfStamper(pdfReader, outputStream);

            for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
                PdfContentByte canvas = pdfStamper.getUnderContent(i);
                UnderContentRemover.write(canvas, underContentByPage.get(i-1));
                canvas.addImage(background);
            }

            pdfStamper.close();
            pdfReader.close();

            sourceWithStampInbetween = outputStream.toByteArray();
        }
        Files.write(new File(RESULT_FOLDER, "PdfLikeVladimirSafonov-WithStampInbetween.pdf").toPath(), sourceWithStampInbetween);
    }

    byte[] createPdfLikeVladimirSafonov() throws DocumentException, IOException {
        Font fontBold = new Font(Font.FontFamily.TIMES_ROMAN, 32f, Font.BOLD);
        Font fontCommon = new Font(Font.FontFamily.TIMES_ROMAN, 16f, Font.NORMAL);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();
            Paragraph title = new Paragraph("Title", fontBold);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("Some text in some paragraph, who knows what we might read here.", fontCommon));

            PdfContentByte canvas = writer.getDirectContentUnder();
            Image background = new Jpeg(getClass().getResource("printer.jpeg"));
            background.scaleAbsolute(PageSize.A4);
            background.setAbsolutePosition(0, 0);
            canvas.addImage(background);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return outputStream.toByteArray();
    }

}
