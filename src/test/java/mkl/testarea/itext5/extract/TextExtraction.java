/**
 * 
 */
package mkl.testarea.itext5.extract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.awt.geom.Rectangle;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.FilteredTextRenderListener;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.RegionTextRenderFilter;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

/**
 * @author mklink
 *
 */
public class TextExtraction
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * Problems with extracting table from PDF
     * http://stackoverflow.com/questions/28828021/problems-with-extracting-table-from-pdf
     * http://www.european-athletics.org/mm/Document/EventsMeetings/General/01/27/52/10/EICH-FinalEntriesforwebsite_Neutral.pdf
     */
    @Test
    public void testEichFinalEntriesForWebsiteNeutral() throws IOException, DocumentException
    {
        InputStream resourceStream = getClass().getResourceAsStream("EICH-FinalEntriesforwebsite_Neutral.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStore(reader, new File(RESULT_FOLDER, "EICH-FinalEntriesforwebsite_Neutral.%s.txt").toString());

            System.out.println("\nText EICH-FinalEntriesforwebsite_Neutral.pdf\n************************");
            System.out.println(content);
            System.out.println("************************");
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    String extractAndStore(PdfReader reader, String format) throws IOException
    {
        StringBuilder builder = new StringBuilder();

        for (int page = 1; page <= reader.getNumberOfPages(); page++)
        {
            String pageText = extract(reader, page);
            Files.write(Paths.get(String.format(format, page)), pageText.getBytes("UTF8"));

            if (page > 1)
                builder.append("\n\n");
            builder.append(pageText);
        }

        return builder.toString();
    }

    String extract(PdfReader reader, int pageNo) throws IOException
    {
        return PdfTextExtractor.getTextFromPage(reader, pageNo, new LocationTextExtractionStrategy());
    }

}
