package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class PhrasesChunksAndFonts {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/54784126/how-can-i-use-regular-and-bold-in-a-single-string-using-itext-android">
     * How can I use regular and bold in a single String using iText Android
     * </a>
     * <p>
     * If you set a font in the Paragraph constructor, the font used
     * for a later added Chunk object is the font of that chunk
     * supplemented by data from the paragraph font in properties
     * not set in the chunk font.
     * </p>
     * <p>
     * The style of the font is a bit field and unfortunately this
     * supplementing in the style field is implemented by means of
     * bit-wise or-ing. Thus, the BOLD flag from the paragraph font
     * is or-ed to the styles of all the chunks added to the
     * paragraph!
     * </p>
     */
    @Test
    public void testLikeKanwarpreetSingh() throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);

        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "PhrasesChunksAndFontsKanwarpreetSingh.pdf")));
        document.open();

        Font semiBoldFont = new Font(Font.FontFamily.TIMES_ROMAN, 16f, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 16f, Font.NORMAL);

        Paragraph paragraphTwo = new Paragraph("Date of Birth: ", semiBoldFont);
        paragraphTwo.add(new Chunk("unknown" , normalFont));
        paragraphTwo.setAlignment(Paragraph.ALIGN_CENTER);

        document.add(paragraphTwo);

        paragraphTwo = new Paragraph("Date of Birth: ", normalFont);
        paragraphTwo.add(new Chunk("unknown" , semiBoldFont));
        paragraphTwo.setAlignment(Paragraph.ALIGN_CENTER);

        document.add(paragraphTwo);

        paragraphTwo = new Paragraph();
        paragraphTwo.add(new Chunk("Date of Birth: ", semiBoldFont));
        paragraphTwo.add(new Chunk("unknown" , normalFont));
        paragraphTwo.setAlignment(Paragraph.ALIGN_CENTER);

        document.add(paragraphTwo);

        paragraphTwo = new Paragraph("", normalFont);
        paragraphTwo.add(new Chunk("Date of Birth: ", semiBoldFont));
        paragraphTwo.add(new Chunk("unknown" , normalFont));
        paragraphTwo.setAlignment(Paragraph.ALIGN_CENTER);

        document.add(paragraphTwo);

        document.close();
    }

}
