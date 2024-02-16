package mkl.testarea.itext5.content;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class TextToPdf {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /** @see #textToPdfLikeNunitoCalzada(String[]) */
    @Test
    public void test10WordsLikeNunitoCalzada() throws DocumentException, IOException {
        String[] words = new String[] {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};
        byte[] pdfBytes = textToPdfLikeNunitoCalzada(words);
        Files.write(new File(RESULT_FOLDER, "10WordsLikeNunitoCalzada.pdf").toPath(), pdfBytes);
    }

    /** @see #textToPdfLikeNunitoCalzada(String[]) */
    @Test
    public void test11WordsLikeNunitoCalzada() throws DocumentException, IOException {
        String[] words = new String[] {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven"};
        byte[] pdfBytes = textToPdfLikeNunitoCalzada(words);
        Files.write(new File(RESULT_FOLDER, "11WordsLikeNunitoCalzada.pdf").toPath(), pdfBytes);
    }

    /** @see #textToPdfLikeNunitoCalzada(String[]) */
    @Test
    public void test12WordsLikeNunitoCalzada() throws DocumentException, IOException {
        String[] words = new String[] {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve"};
        byte[] pdfBytes = textToPdfLikeNunitoCalzada(words);
        Files.write(new File(RESULT_FOLDER, "12WordsLikeNunitoCalzada.pdf").toPath(), pdfBytes);
    }

    /**
     * <a href="https://stackoverflow.com/questions/78001852/itext5-core-write-text-into-paragraph">
     * itext5-core -> Write text into paragraph
     * </a>
     * <p>
     * This is essentially the OP's code which is tested with arrays of 10 to 12 strings.
     * For 10 strings all these strings end up in <code>newSentence</code>, for 11 strings
     * none do, and for 12 strings the last one does.
     * </p>
     * <p>
     * I cannot reproduce the issue of the OP, everything works as expected.
     * </p>
     * @see #test10WordsLikeNunitoCalzada()
     * @see #test11WordsLikeNunitoCalzada()
     * @see #test12WordsLikeNunitoCalzada()
     */
    byte[] textToPdfLikeNunitoCalzada(String[] sentences) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Logger log = Logger.getLogger(getClass().getName());

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        Font descFont = new Font();
        Image backgroundPageImage = Image.getInstance(1, 1, 1, 8, new byte[] {(byte)128});
        int numberOfWords = 1;
        int numberOfLines = 1;
        StringBuilder newSentence = new StringBuilder();

        // Iterate through each sentence
        for (String sentence : sentences) {

            newSentence.append(sentence).append(" ");
            if (numberOfWords++ % 11 == 0) {
                newSentence.append("\n");
                Paragraph descParagraph = new Paragraph(newSentence.toString(), descFont);
                document.add(descParagraph);

                if (numberOfLines++ % 30 == 0) {
                    document.add(new Paragraph(" "));
                    document.newPage();
                    writer.getDirectContentUnder().addImage(backgroundPageImage);
                }

                newSentence = new StringBuilder();
            }
        }

        log.info(" ");
        log.info("<<-=======================================================================================>>");
        log.info("newSentence: " + newSentence.toString());
        log.info("<<-=======================================================================================>>");
        log.info(" ");

        if (newSentence.length() > 0) {
            log.info(" I AM HERE !!!!!");
            newSentence.append("\n");
            Paragraph descParagraph = new Paragraph(newSentence.toString(), descFont);
            document.add(descParagraph);
        }

        document.close();
        return baos.toByteArray();
    }
}
