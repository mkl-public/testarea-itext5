package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.io.StreamUtil;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class ContentInCircle {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/54888957/insert-multiple-paragraphs-and-images-inside-a-circle-using-java-itext">
     * Insert multiple paragraphs and images inside a circle using Java iText
     * </a>
     * <p>
     * Corrected the obvious errors in the OP's code: Stopped drawing
     * text white on white, pushed <code>ColumnText.showTextAligned</code>
     * out of text object, actually added image to page content. 
     * </p>
     */
    @Test
    public void testLikeRKumar() throws IOException, DocumentException {
//        String printingPath = "CD_label.pdf"; 
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "circle-with-content-RKumar.pdf")));
        document.open();

        PdfContentByte cb = writer.getDirectContent(); 
//        cb.setRGBColorFill(0xFF, 0xFF, 0xFF);
        BaseColor colorval = new BaseColor(102,178,255);
        cb.setColorStroke(colorval); 
        cb.circle(300.0f, 650.0f, 150.0f);
        cb.circle(300.0f, 650.0f, 20.0f); 
        cb.stroke();


        cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA,BaseFont.CP1257,BaseFont.EMBEDDED), 10); 
        cb.beginText(); 
        cb.resetRGBColorStroke();
        cb.setTextMatrix(320, 420); 
        cb.showText("Text inside cd");
        cb.endText();

        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,new Phrase("Hello itext"),50, 700, 0);
//        cb.endText();

        //Image img = Image.getInstance("Symbol.png");
        Image img = Image.getInstance(StreamUtil.inputStreamToArray(getClass().getResourceAsStream("2x2colored.png")));
        img.setAbsolutePosition(270f, 740f); 
        img.scaleAbsolute(60, 34);
        cb.addImage(img);

        document.close();
    }

    /**
     * <a href="https://stackoverflow.com/questions/54888957/insert-multiple-paragraphs-and-images-inside-a-circle-using-java-itext">
     * Insert multiple paragraphs and images inside a circle using Java iText
     * </a>
     * <p>
     * Inspired by the question but not really for its answer, this
     * test shows how to use {@link ColumnText} to draw text inside
     * a circular area.
     * </p>
     */
    @Test
    public void testCircleWithContent1() throws IOException, DocumentException {
        Image img = Image.getInstance(StreamUtil.inputStreamToArray(getClass().getResourceAsStream("2x2colored.png")));
        img.scaleAbsolute(45, 45);

        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document,new FileOutputStream(new File(RESULT_FOLDER, "circle-with-content-1.pdf")));
        document.open();

        PdfContentByte canvas = writer.getDirectContent();

        canvas.saveState();
        BaseColor colorval = new BaseColor(102,178,255);
        canvas.setColorStroke(colorval); 
        canvas.circle(300.0f, 650.0f, 150.0f);
        canvas.stroke();
        canvas.restoreState();

        ColumnText columnText = new ColumnText(canvas);
        setFullCircle(columnText, 300, 650, 150, 10);
        columnText.addText(new Chunk("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."));
        columnText.go();

        document.close();
    }

    /**
     * @see #testCircleWithContent1()
     */
    void setFullCircle(ColumnText columnText, float x, float y, float r, int steps) {
        float[] left = new float[2 * steps];
        float[] right = new float[2 * steps];

        for (int step = 0; step < steps; step++) {
            double angle = step * Math.PI / (steps - 1);
            float xDiff = (float) (r * Math.sin(angle));
            float yDiff = (float) (r * Math.cos(angle));
            left[2 * step] = x - xDiff;
            left[2 * step + 1] = y + yDiff;
            right[2 * step] = x + xDiff;
            right[2 * step + 1] = y + yDiff;
        }

        columnText.setColumns(left, right);
    }
}
