package mkl.testarea.itext5.content;

import static com.itextpdf.text.pdf.parser.Vector.I1;
import static com.itextpdf.text.pdf.parser.Vector.I2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.Vector;

import mkl.testarea.itext5.content.SimpleTextRemover.Glyph;

/**
 * <a href="https://stackoverflow.com/questions/57308588/replace-text-inside-a-pdf-file-using-itext">
 * Replace text inside a PDF file using iText
 * </a>
 * <p>
 * This test class checks the {@link SimpleTextRemover} content
 * editor class. It has certain shortcomings documented in the
 * class itself, but it can already do its job in many PDFs.
 * </p>
 * 
 * @author mkl
 */
public class RemovePageTextContent {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/57308588/replace-text-inside-a-pdf-file-using-itext">
     * Replace text inside a PDF file using iText
     * </a>
     * <p>
     * This test applies the {@link SimpleTextRemover} to a simple
     * test document and removes its occurrences of "Test".
     * </p>
     */
    @Test
    public void testRemoveTestFromTest() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "test-remove-Test.pdf")))
        {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            SimpleTextRemover remover = new SimpleTextRemover();

            System.out.printf("\ntest.pdf - Test\n");
            for (int i = 1; i <= pdfReader.getNumberOfPages(); i++)
            {
                System.out.printf("Page %d:\n", i);
                List<List<Glyph>> matches = remover.remove(pdfStamper, i, "Test");
                for (List<Glyph> match : matches) {
                    Glyph first = match.get(0);
                    Vector baseStart = first.base.getStartPoint();
                    Glyph last = match.get(match.size()-1);
                    Vector baseEnd = last.base.getEndPoint();
                    System.out.printf("  Match from (%3.1f %3.1f) to (%3.1f %3.1f)\n", baseStart.get(I1), baseStart.get(I2), baseEnd.get(I1), baseEnd.get(I2));
                }
            }
            
            pdfStamper.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/57308588/replace-text-inside-a-pdf-file-using-itext">
     * Replace text inside a PDF file using iText
     * </a>
     * <p>
     * This test applies the {@link SimpleTextRemover} to a simple
     * test document and removes its occurrences of "stte".
     * </p>
     * <p>
     * The document text ends with "test", i.e. with a partial match.
     * Thus, this test checks whether final ignoring of a partial
     * match and flushing of the remaining cached operations works
     * properly. At the same time, though, it illustrates that the
     * remover ignores gaps because it recognizes a match in the
     * middle of "test test".
     * </p>
     */
    @Test
    public void testRemovestteFromTest() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "test-remove-stte.pdf")))
        {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            SimpleTextRemover remover = new SimpleTextRemover();

            System.out.printf("\ntest.pdf - stte\n");
            for (int i = 1; i <= pdfReader.getNumberOfPages(); i++)
            {
                System.out.printf("Page %d:\n", i);
                List<List<Glyph>> matches = remover.remove(pdfStamper, i, "stte");
                for (List<Glyph> match : matches) {
                    Glyph first = match.get(0);
                    Vector baseStart = first.base.getStartPoint();
                    Glyph last = match.get(match.size()-1);
                    Vector baseEnd = last.base.getEndPoint();
                    System.out.printf("  Match from (%3.1f %3.1f) to (%3.1f %3.1f)\n", baseStart.get(I1), baseStart.get(I2), baseEnd.get(I1), baseEnd.get(I2));
                }
            }
            
            pdfStamper.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/57308588/replace-text-inside-a-pdf-file-using-itext">
     * Replace text inside a PDF file using iText
     * </a>
     * <p>
     * This test applies the {@link SimpleTextRemover} to a test
     * document and removes its occurrences of "é".
     * </p>
     * <p>
     * The document makes use of text showing operators with side
     * effects (namely <b>'</b>). Thus, this test checks whether
     * the remover properly adds the former side effects when
     * replacing <b>'</b> by <b>TJ</b>.
     * </p>
     */
    @Test
    public void testRemoveéFromTestSteveB() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/testSteveB.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "testSteveB-remove-é.pdf")))
        {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            SimpleTextRemover remover = new SimpleTextRemover();

            System.out.printf("\ntestSteveB.pdf - é\n");
            for (int i = 1; i <= pdfReader.getNumberOfPages(); i++)
            {
                System.out.printf("Page %d:\n", i);
                List<List<Glyph>> matches = remover.remove(pdfStamper, i, "é");
                for (List<Glyph> match : matches) {
                    Glyph first = match.get(0);
                    Vector baseStart = first.base.getStartPoint();
                    Glyph last = match.get(match.size()-1);
                    Vector baseEnd = last.base.getEndPoint();
                    System.out.printf("  Match from (%3.1f %3.1f) to (%3.1f %3.1f)\n", baseStart.get(I1), baseStart.get(I2), baseEnd.get(I1), baseEnd.get(I2));
                }
            }
            
            pdfStamper.close();
        }
    }
}
