package mkl.testarea.itext5.form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.TextField;

/**
 * @author mkl
 */
public class SetFieldFont {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/61072662/itext-5-textfield-using-chinese-font-with-bold-and-italic">
     * itext 5 TextField using chinese font with bold and italic
     * </a>
     * <p>
     * This test checks font setting on form fields. In particular it
     * verifies that the color of a font is not used for a text field
     * having the base font of that font set.
     * </p>
     */
    @Test
    public void testLikeZiv() throws IOException, DocumentException {
        try (   FileOutputStream fos = new FileOutputStream(new File(RESULT_FOLDER, "SetFieldFontLikeZiv.pdf")) ) {
            Document document = new Document();
            PdfWriter pdfWriter = PdfWriter.getInstance(document, fos);
            document.open();

            Font fontGabriolaGreen = FontFactory.getFont("c:\\Windows\\Fonts\\Gabriola.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    10, 3, BaseColor.GREEN);
            Font fontGabriolaRed = FontFactory.getFont("c:\\Windows\\Fonts\\Gabriola.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    10, 3, BaseColor.RED);

            TextField greenField = new TextField(pdfWriter, new Rectangle(100, 600, 250, 666), "GreenField");
            greenField.setFont(fontGabriolaGreen.getBaseFont());
            pdfWriter.addAnnotation(greenField.getTextField());

            TextField redField = new TextField(pdfWriter, new Rectangle(300, 600, 450, 666), "RedField");
            redField.setFont(fontGabriolaRed.getBaseFont());
            pdfWriter.addAnnotation(redField.getTextField());

            Font fontGothicGreen = FontFactory.getFont("c:\\Windows\\Fonts\\GOTHICBI.TTF", BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    10, 3, BaseColor.GREEN);
            Font fontGothicRed = FontFactory.getFont("c:\\Windows\\Fonts\\GOTHICBI.TTF", BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    10, 3, BaseColor.RED);

            TextField greenFieldGothic = new TextField(pdfWriter, new Rectangle(100, 500, 250, 566), "GreenFieldGothic");
            greenFieldGothic.setFont(fontGothicGreen.getBaseFont());
            greenFieldGothic.setTextColor(fontGothicGreen.getColor());
            pdfWriter.addAnnotation(greenFieldGothic.getTextField());

            TextField redFieldGothic = new TextField(pdfWriter, new Rectangle(300, 500, 450, 566), "RedFieldGothic");
            redFieldGothic.setFont(fontGothicRed.getBaseFont());
            redFieldGothic.setTextColor(fontGothicRed.getColor());
            pdfWriter.addAnnotation(redFieldGothic.getTextField());

            document.add(new Paragraph("green", fontGabriolaGreen));
            document.add(new Paragraph("red", fontGabriolaRed));
            document.add(new Paragraph("green", fontGothicGreen));
            document.add(new Paragraph("red", fontGothicRed));
            document.close();
        }
    }

}
