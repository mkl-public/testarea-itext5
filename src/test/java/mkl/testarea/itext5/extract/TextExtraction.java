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
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

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
     * <a href="http://stackoverflow.com/questions/32815291/itextsharp-cant-read-numbers-in-this-pdf">
     * iTextSharp can't read numbers in this PDF
     * </a>
     * <br/>
     * <a href="http://www.bnpparibas-ip.sg/doc/fact/FACTSHEET05ENSG07100715FO02332302.pdf">
     * FACTSHEET05ENSG07100715FO02332302.pdf
     * </a>
     * <p>
     * Indeed, neither iText nor iTextSharp could extract certain digits at the time
     * of that question. Meanwhile, though, the issue seems fixed.
     * </p>
     */
    @Test
    public void testFACTSHEET05ENSG07100715FO02332302() throws IOException, DocumentException
    {
        InputStream resourceStream = getClass().getResourceAsStream("FACTSHEET05ENSG07100715FO02332302.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStoreSimple(reader, new File(RESULT_FOLDER, "FACTSHEET05ENSG07100715FO02332302.%s.txt").toString());

            System.out.println("\nText FACTSHEET05ENSG07100715FO02332302.pdf\n************************");
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
     * <a href="http://stackoverflow.com/questions/32081406/why-is-itext-failing-to-extract-this-text">
     * Why is iText failing to extract this text?
     * </a>
     * <p>
     * As Bruno indicated, current iText does not have an issue here.
     * </p>
     */
    @Test
    public void testA00031() throws IOException, DocumentException
    {
        InputStream resourceStream = getClass().getResourceAsStream("A00031.PDF");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStore(reader, new File(RESULT_FOLDER, "A00031.%s.txt").toString());

            System.out.println("\nText A00031.pdf\n************************");
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

    /**
     * <a href="http://stackoverflow.com/questions/30536032/exceptionconverter-com-itextpdf-text-pdf-parser-inlineimageutilsinlineimagepars">
     * ExceptionConverter com.itextpdf.text.pdf.parser.InlineImageUtils$InlineImageParseException: Could not find image data or EI 420
     * </a>
     * <br>
     * <a href="https://www.dropbox.com/s/4l4ioqzpcca05vc/Understanding%20the%20High%20Photocatalytic%20Activity%20of%20%28B%2C%20Ag%29-Codopeda312205c_si_001.pdf?dl=0">
     * "Understanding the High Photocatalytic Activity of (B, Ag)-Codopeda312205c_si_001.pdf"
     * </a>
     * 
     * Indeed, PdfReader.decodeBytes() throws an exception because it retrieves a PDF NULL as PdfLiteral, not as null oder PdfNull.
     * 
     * @throws IOException
     * @throws DocumentException
     */
    @Test
    public void testUnderstandingTheHighPhotocatalyticActivity() throws IOException, DocumentException
    {
        InputStream resourceStream = getClass().getResourceAsStream("Understanding the High Photocatalytic Activity of (B, Ag)-Codopeda312205c_si_001.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStore(reader, new File(RESULT_FOLDER, "Understanding the High Photocatalytic Activity of (B, Ag)-Codopeda312205c_si_001.%s.txt").toString());

            System.out.println("\nText Understanding the High Photocatalytic Activity of (B, Ag)-Codopeda312205c_si_001.pdf\n************************");
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
     * <a href="http://stackoverflow.com/questions/32014589/how-to-read-data-from-table-structured-pdf-using-itextsharp">
     * How to read data from table-structured PDF using itextsharp?
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/jwsuu6mz9ez84ss/sampleFile.pdf?dl=0">
     * sampleFile.pdf
     * </a>
     * <p>
     * By explicitly using the {@link SimpleTextExtractionStrategy} one gets the same text
     * as with PDFBox.
     * </p>
     * 
     * @see mkl.testarea.pdfbox1.extract.ExtractText
     */
    @Test
    public void testSampleFile() throws IOException, DocumentException
    {
        InputStream resourceStream = getClass().getResourceAsStream("sampleFile.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStoreSimple(reader, new File(RESULT_FOLDER, "sampleFile.%s.txt").toString());

            System.out.println("\nText (simple strategy) sampleFile.pdf \n************************");
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

    String extractAndStoreSimple(PdfReader reader, String format) throws IOException
    {
        StringBuilder builder = new StringBuilder();

        for (int page = 1; page <= reader.getNumberOfPages(); page++)
        {
            String pageText = extractSimple(reader, page);
            Files.write(Paths.get(String.format(format, page)), pageText.getBytes("UTF8"));

            if (page > 1)
                builder.append("\n\n");
            builder.append(pageText);
        }

        return builder.toString();
    }

    String extractSimple(PdfReader reader, int pageNo) throws IOException
    {
        return PdfTextExtractor.getTextFromPage(reader, pageNo, new SimpleTextExtractionStrategy()
        {
            boolean empty = true;

            @Override
            public void beginTextBlock()
            {
                if (!empty)
                    appendTextChunk("<BLOCK>");
                super.beginTextBlock();
            }

            @Override
            public void endTextBlock()
            {
                if (!empty)
                    appendTextChunk("</BLOCK>\n");
                super.endTextBlock();
            }

            @Override
            public String getResultantText()
            {
                if (empty)
                    return super.getResultantText();
                else
                    return "<BLOCK>" + super.getResultantText();
            }

            @Override
            public void renderText(TextRenderInfo renderInfo)
            {
                empty = false;
                super.renderText(renderInfo);
            }
            
        });
    }
}
