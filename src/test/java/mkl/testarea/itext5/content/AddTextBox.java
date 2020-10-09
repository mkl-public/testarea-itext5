package mkl.testarea.itext5.content;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class AddTextBox {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/64239746/how-to-create-rectangle-vertically-and-add-text-to-it-in-pdf-using-itext-2-1-7">
     * How to create rectangle vertically and add text to it in PDF using itext 2.1.7
     * </a>
     * <p>
     * This test shows how to add text in a rotated, framed box. This code
     * should equally well work with iText 2.1.7 after switching packages.
     * </p>
     */
    @Test
    public void testRotatedBoxForAbbas() throws DocumentException, IOException {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        writer.setPageEmpty(false);
        document.close();

        float width = Utilities.millimetersToPoints(10);
        float height = Utilities.millimetersToPoints(100);
        float x = Utilities.millimetersToPoints(15);
        float y = Utilities.millimetersToPoints(150);
        float fontHeight = Utilities.millimetersToPoints(4);
        String content = "Some text to fill the box. There's nothing really to say, just a box to fill. So let's fill the box.";

        PdfReader reader = new PdfReader(baos.toByteArray());
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, "RotatedBoxForAbbas.pdf")));
        Rectangle cropBox = reader.getCropBox(1);
        PdfContentByte canvas = stamper.getOverContent(1);
        canvas.concatCTM(0, 1, -1, 0, cropBox.getLeft() + x + width, cropBox.getBottom() + y);
        canvas.rectangle(0, 0, height, width);
        canvas.stroke();
        ColumnText columnText = new ColumnText(canvas);
        columnText.addText(new Chunk(content, new Font(FontFamily.HELVETICA, fontHeight)));
        columnText.setLeading(fontHeight);
        columnText.setSimpleColumn(2, 0, height - 4, width);
        columnText.go();
        stamper.close();
    }

}
