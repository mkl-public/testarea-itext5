package mkl.testarea.itext5.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

/**
 * This test focuses on text extraction issues.
 * 
 * @author mkl
 */
public class TextExtraction
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

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

    /**
     * <a href="http://stackoverflow.com/questions/29209553/itextsharp-pdftextextractor-gettextfrompage-throwing-nullreferenceexception">
     * iTextSharp PdfTextExtractor GetTextFromPage Throwing NullReferenceException
     * </a>
     * 
     * Test using a valid copy of stockQuotes_03232015.pdf from
     * http://www.pse.com.ph/stockMarket/marketInfo-marketActivity.html?tab=4
     */
    @Test
    public void testStockQuotes_03232015() throws IOException, DocumentException
    {
        InputStream resourceStream = getClass().getResourceAsStream("stockQuotes_03232015.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStore(reader, new File(RESULT_FOLDER, "stockQuotes_03232015.%s.txt").toString());

            System.out.println("\nText stockQuotes_03232015.pdf\n************************");
            System.out.println(content);
            System.out.println("************************");
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/29209553/itextsharp-pdftextextractor-gettextfrompage-throwing-nullreferenceexception">
     * iTextSharp PdfTextExtractor GetTextFromPage Throwing NullReferenceException
     * </a>
     * 
     * Test using an incomplete, invalid copy of stockQuotes_03232015.pdf from
     * http://www.pse.com.ph/stockMarket/marketInfo-marketActivity.html?tab=4
     */
    @Test
    public void testStockQuotes_03232015_Incomplete() throws IOException, DocumentException
    {
        InputStream resourceStream = getClass().getResourceAsStream("stockQuotes_03232015-incomplete.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStore(reader, new File(RESULT_FOLDER, "stockQuotes_03232015-incomplete.%s.txt").toString());

            System.out.println("\nText stockQuotes_03232015-incomplete.pdf\n************************");
            System.out.println(content);
            System.out.println("************************");
        }
        catch (ExceptionConverter e)
        {
            System.err.println("\nException for stockQuotes_03232015-incomplete.pdf\n************************");
            e.printStackTrace();
            System.err.println("************************");
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/29300324/itext-pdf-bad-character-conversion">
     * iText PDF bad character conversion
     * </a>
     * 
     * Indeed, Information in the PDF are not good for immediate text extraction.
     */
    @Test
    public void testBolletta_Anonima() throws IOException, DocumentException
    {
        InputStream resourceStream = getClass().getResourceAsStream("Bolletta_Anonima.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStore(reader, new File(RESULT_FOLDER, "Bolletta_Anonima.%s.txt").toString());

            System.out.println("\nText Bolletta_Anonima.pdf\n************************");
            System.out.println(content);
            System.out.println("************************");
            for (char c: content.toCharArray())
            {
                if (c == '\r' || c == '\n' || c==' ')
                    System.out.print((char)(c));
                else
                    System.out.print((char)(c+0x1c));
            }
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/30242538/pdf-contains-text-but-itextpdf-dont-see-it">
     * PDF contains text, but ITextPDF dont see it
     * </a>
     * 
     * Indeed, Information in the PDF are not good for immediate text extraction.
     */
    @Test
    public void testTestLukasRr() throws IOException, DocumentException
    {
        InputStream resourceStream = getClass().getResourceAsStream("testLukasRr.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStore(reader, new File(RESULT_FOLDER, "testLukasRr.%s.txt").toString());

            System.out.println("\nText testLukasRr.pdf\n************************");
            System.out.println(content);
            System.out.println("************************");
            for (char c: content.toCharArray())
            {
                if (c == '\r' || c == '\n' || c==' ')
                    System.out.print((char)(c));
                else
                    System.out.print((char)(c+0x1c));
            }
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
