package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class EmptyParagraphs {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/55465718/why-does-java-delete-the-blank-space-between-paragraphs-during-conversion-to-pdf">
     * Why does Java delete the blank space between paragraphs during conversion to PDF?
     * </a>
     * <p>
     * This test shows what happens if one creates a {@link Paragraph}
     * with an empty string.
     * </p>
     */
    @Test
    public void testEmptyParagraphsForFazriBadri() throws IOException, DocumentException {
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, new FileOutputStream(new File(RESULT_FOLDER, "EmptyParagraphsForFazriBadri.pdf")));
        doc.open();

        doc.add(new Paragraph("1 Between this line and line 3 there is an empty paragraph."));
        doc.add(new Paragraph(""));
        doc.add(new Paragraph("3 This is line 3."));
        doc.add(new Paragraph("4 Between this line and line 6 there is a paragraph with only a space."));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("6 This is line 6."));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));
        Font normal = new Font(FontFamily.COURIER);
        doc.add(new Paragraph("{\\rtf1\\ansi{\\fonttbl\\f0\\fswiss Helvetica;}\\f0\\pard", normal));
        doc.add(new Paragraph("This is some {\\b bold} text.\\par", normal));
        doc.add(new Paragraph("}", normal));
        doc.close();
    }

}
