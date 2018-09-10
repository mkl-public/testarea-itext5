package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class TestRotatedImagePosition {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/52153094/itext-image-setrotationdegrees-not-keeping-a-consistent-origin">
     * iText image.setRotationDegrees() not keeping a consistent origin
     * </a>
     * <p>
     * This is the original code of the OP. Indeed, the rotation center to get one
     * image from the other is not an obvious corner.
     * </p>
     * <p>
     * The reason: The Image is not positioned at the given coordinates and then rotated
     * around an obvious point. Instead the Image is rotated, then its bounding box is
     * determined, and finally this bounding box is positioned to have its lower left
     * at the given coordinates. This is more obvious with more rectangles, cf. 
     * {@link #testPositionsLikeSujayJAndMore()}.
     * </p>
     */
    @Test
    public void testPositionsLikeSujayJ() throws FileNotFoundException, DocumentException {
     // step 1
        Rectangle pageSize = PageSize.A4;
        Document document = new Document(pageSize);

        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "RotatedImageLikeSujayJ.pdf")));

        // step 3
        document.open();

        // step 4
        float boxWidth = 200;
        float boxHeight = 50;
        float xStart = pageSize.getWidth()/2;
        float yStart = pageSize.getHeight()/2;

        // Add one filled rectangle rotated 90 degrees
        {
            PdfContentByte canvas = writer.getDirectContent();
            PdfTemplate textTemplate = canvas.createTemplate(boxWidth, boxHeight);
            textTemplate.saveState();
            textTemplate.setColorFill(BaseColor.RED);
            textTemplate.rectangle(0, 0, boxWidth, boxWidth);
            textTemplate.fill();
            textTemplate.restoreState();

            Image img = Image.getInstance(textTemplate);
            img.setInterpolation(true);
            img.scaleAbsolute(boxWidth, boxHeight);
            img.setAbsolutePosition(xStart, yStart);
            img.setRotationDegrees(90);
            writer.getDirectContent().addImage(img);
        }

        // And another rotated 30 degrees
        {
            PdfContentByte canvas = writer.getDirectContent();
            PdfTemplate textTemplate = canvas.createTemplate(boxWidth, boxHeight);
            textTemplate.saveState();
            textTemplate.setColorFill(BaseColor.BLACK);
            textTemplate.rectangle(0, 0, boxWidth, boxWidth);
            textTemplate.fill();
            textTemplate.restoreState();

            Image img = Image.getInstance(textTemplate);
            img.setInterpolation(true);
            img.scaleAbsolute(boxWidth, boxHeight);
            img.setAbsolutePosition(xStart, yStart);
            img.setRotationDegrees(30);
            writer.getDirectContent().addImage(img);
        }

        // step 5
        document.close();
    }

    /**
     * <a href="https://stackoverflow.com/questions/52153094/itext-image-setrotationdegrees-not-keeping-a-consistent-origin">
     * iText image.setRotationDegrees() not keeping a consistent origin
     * </a>
     * <p>
     * As {@link #testPositionsLikeSujayJ()} shows, the rotation center to get one
     * image from the other is not an obvious corner.
     * </p>
     * <p>
     * The reason: The Image is not positioned at the given coordinates and then rotated
     * around an obvious point. Instead the Image is rotated, then its bounding box is
     * determined, and finally this bounding box is positioned to have its lower left
     * at the given coordinates. This is more obvious with more rectangles, as this
     * method shows.
     * </p>
     * @see #addRotatedImage(PdfWriter, float, float, BaseColor, float, float, float)
     */
    @Test
    public void testPositionsLikeSujayJAndMore() throws FileNotFoundException, DocumentException {
     // step 1
        Rectangle pageSize = PageSize.A4;
        Document document = new Document(pageSize);

        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "RotatedImageLikeSujayJAndMore.pdf")));

        // step 3
        document.open();

        // step 4
        float boxWidth = 200;
        float boxHeight = 50;
        float xStart = pageSize.getWidth()/2;
        float yStart = pageSize.getHeight()/2;

        addRotatedImage(writer, boxWidth, boxHeight, BaseColor.RED, xStart, yStart, 90);
        addRotatedImage(writer, boxWidth, boxHeight, BaseColor.BLUE, xStart, yStart, 75);
        addRotatedImage(writer, boxWidth, boxHeight, BaseColor.CYAN, xStart, yStart, 60);
        addRotatedImage(writer, boxWidth, boxHeight, BaseColor.GREEN, xStart, yStart, 45);
        addRotatedImage(writer, boxWidth, boxHeight, BaseColor.BLACK, xStart, yStart, 30);
        addRotatedImage(writer, boxWidth, boxHeight, BaseColor.MAGENTA, xStart, yStart, 15);
        addRotatedImage(writer, boxWidth, boxHeight, BaseColor.ORANGE, xStart, yStart, 00);

        // step 5
        document.close();
    }

    /**
     * @see #testPositionsLikeSujayJAndMore()
     */
    void addRotatedImage(PdfWriter writer, float boxWidth, float boxHeight, BaseColor color, float xStart, float yStart, float deg) throws DocumentException {
        PdfContentByte canvas = writer.getDirectContent();
        PdfTemplate textTemplate = canvas.createTemplate(boxWidth, boxHeight);
        textTemplate.saveState();
        textTemplate.setColorStroke(color);
        textTemplate.rectangle(0, 0, boxWidth, boxHeight);
        textTemplate.stroke();
        textTemplate.restoreState();

        Image img = Image.getInstance(textTemplate);
        img.setInterpolation(true);
        img.scaleAbsolute(boxWidth, boxHeight);
        img.setAbsolutePosition(xStart, yStart);
        img.setRotationDegrees(deg);
        writer.getDirectContent().addImage(img);
    }
}
