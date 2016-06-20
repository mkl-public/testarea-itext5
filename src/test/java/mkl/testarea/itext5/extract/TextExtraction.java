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
import com.itextpdf.text.pdf.parser.FilteredTextRenderListener;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.RenderFilter;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
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
     * <a href="http://stackoverflow.com/questions/37919301/itext-textextracting-example-not-working">
     * itext: Textextracting example not working
     * </a>
     * <br/>
     * <a href="http://s000.tinyupload.com/index.php?file_id=04085076377229572360">
     * HTR_Reichsgericht.pdf
     * </a>
     * <p>
     * The issue cannot be reproduced. Analyzing the given stacktrace one is led to assume the
     * OP uses the number of a non-existing page.
     * </p>
     */
    @Test
    public void testHTR_Reichsgericht() throws IOException, DocumentException
    {
        InputStream resourceStream = getClass().getResourceAsStream("HTR_Reichsgericht.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStoreSimple(reader, new File(RESULT_FOLDER, "HTR_Reichsgericht.%s.txt").toString());

            System.out.println("\nText HTR_Reichsgericht.pdf\n************************");
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
     * <a href="http://itext.2136553.n4.nabble.com/iText-help-resources-tt4660980.html">
     * [iText-questions] iText help resources?
     * </a>
     * <br/>
     * <a href="http://itext.2136553.n4.nabble.com/attachment/4660980/0/testin.pdf">
     * testin.pdf
     * </a>
     * <p>
     * Indeed, the tables cannot be extracted. Further analysis shows that the text
     * in the tables uses type 3 fonts with an ad-hoc encoding missing any mapping
     * to Unicode.
     * </p>
     */
    @Test
    public void testTestin() throws IOException, DocumentException
    {
        InputStream resourceStream = getClass().getResourceAsStream("testin.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStoreSimple(reader, new File(RESULT_FOLDER, "testin.%s.txt").toString());

            System.out.println("\nText testin.pdf\n************************");
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
    public void testA00031() throws Exception
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
     * <a href="http://stackoverflow.com/questions/37439613/itextpdf-insert-space-beetwen-7-and-dot-after-extract-text">
     * itextpdf insert space beetwen 7 and dot after extract text
     * </a>
     * <br/>
     * <a href="http://185.49.12.119/~pogdan/7spacedot/monitor_2016_99.pdf">
     * monitor_2016_99.pdf
     * </a>
     * <p>
     * iText inserts spaces whenever there is a gap between two consecutive text chunks
     * which is larger than a certain amount, or if two consecutive text chunks overlap.
     * It does so to signal that the chunks do not follow each other in a normal way.
     * </p>
     * <p>
     * In case of this document a dot following a seven often is moved left as far as
     * possible without touching the seven so that the character bounding boxes overlap.
     * </p>
     */
    @Test
    public void testMonitor_2016_99() throws Exception
    {
        InputStream resourceStream = getClass().getResourceAsStream("monitor_2016_99.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStore(reader, new File(RESULT_FOLDER, "monitor_2016_99.%s.txt").toString());

            System.out.println("\nText monitor_2016_99.pdf\n************************");
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
    public void testEichFinalEntriesForWebsiteNeutral() throws Exception
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
    public void testStockQuotes_03232015() throws Exception
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
    public void testStockQuotes_03232015_Incomplete() throws Exception
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
    public void testBolletta_Anonima() throws Exception
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
    public void testTestLukasRr() throws Exception
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
    public void testUnderstandingTheHighPhotocatalyticActivity() throws Exception
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
     * <a href="http://stackoverflow.com/questions/37262087/avoid-reading-hidden-text-from-pdf">
     * Avoid reading hidden text from PDF
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B-JlUfbplwmhUjN3QWExeUVNclU/view?usp=sharing">
     * demo.pdf
     * </a>
     * 
     * <p>
     * The extra, invisible text turns out to be drawn in rendering mode 3 at the origin.
     * Filtering by text rendering mode gets rid of it.
     * </p>
     */
    @Test
    public void testDemo() throws Exception
    {
        InputStream resourceStream = getClass().getResourceAsStream("demo.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStore(reader, new File(RESULT_FOLDER, "demo.%s.txt").toString());
            RenderFilter modeFilter = new RenderFilter()
            {
                public boolean allowText(TextRenderInfo renderInfo){
                    return renderInfo.getTextRenderMode() != 3;
                }
            };
            String filteredContent = extractAndStore(reader, new File(RESULT_FOLDER, "demo.filtered.%s.txt").toString(), modeFilter);

            System.out.println("\nText demo.pdf\n************************");
            System.out.println(content);
            System.out.println("\n*filtered");
            System.out.println(filteredContent);
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

    /**
     * <a href="http://stackoverflow.com/questions/35344982/itext-extracted-text-from-pdf-file-using-locationtextextractionstrategy-is-in-w">
     * iText: Extracted text from pdf file using LocationTextExtractionStrategy is in wrong order
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/kl2s6038u51gx2q/location_text_extraction_test.pdf?dl=0">
     * location_text_extraction_test.pdf
     * </a>
     * <p>
     * Indeed, the {@link LocationTextExtractionStrategy} returns the headers in the wrong order.
     * This is due to slightly different y coordinates of them.
     * </p>
     * <p>
     * The {@link HorizontalTextExtractionStrategy2} returns the headers and actually the whole table
     * correctly. Unfortunately it fails where there are overlapping lines in side-by-side columns,
     * in this case e.g. for the invoice recipient address.
     * </p>
     */
    @Test
    public void testLocation_text_extraction_test() throws Exception
    {
        InputStream resourceStream = getClass().getResourceAsStream("location_text_extraction_test.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStore(reader, new File(RESULT_FOLDER, "location_text_extraction_test.%s.txt").toString());
            String horizontalContent = extractAndStore(reader, new File(RESULT_FOLDER, "location_text_extraction_test.%s.txt").toString(), HorizontalTextExtractionStrategy2.class);

            System.out.println("\nText (location strategy) location_text_extraction_test.pdf \n************************");
            System.out.println(content);
            System.out.println("\nText (horizontal strategy) location_text_extraction_test.pdf \n************************");
            System.out.println(horizontalContent);
            System.out.println("************************");
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/37307289/text-from-pdf-parsing-differently-using-itext">
     * Text from PDF parsing differently using iText
     * </a>
     * <br/>
     * <a href="https://github.com/Baddy247/Shared-Files/raw/master/test%20pdf.pdf">
     * test pdf.pdf
     * </a>
     * <p>
     * While the rows look like they are they are at a constant level each, they actually are not.
     * The 'XXX...:' and 'TOTAL :' parts are at y coordinates 469.45, 457.95, and 446.45 while the
     * '#..', '1', and '2' parts are at y coordinates 468.65, 457.15, and 445.65.
     * </p>
     * <p>
     * To consider horizontal text to be on the same line, iText text extraction using the default
     * text extraction strategy ({@link LocationTextExtractionStrategy}) requires the y coordinates
     * to be the same after casting to int. (Actually this is somewhat simplified, for the whole
     * picture look at {@link LocationTextExtractionStrategy.TextChunkLocationDefaultImp}).
     * </p>
     * <p>
     * In the case at hand this only is the case for the middle row.
     * </p>
     * <p>
     * The {@link HorizontalTextExtractionStrategy2} on the other hand recognized each row as a
     * single line.
     * </p>
     */
    @Test
    public void testTest_pdf() throws Exception
    {
        InputStream resourceStream = getClass().getResourceAsStream("test pdf.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStore(reader, new File(RESULT_FOLDER, "test pdf.%s.txt").toString());
            String horizontalContent = extractAndStore(reader, new File(RESULT_FOLDER, "test pdf.HOR.%s.txt").toString(), HorizontalTextExtractionStrategy2.class);

            System.out.println("\nText (location strategy) test pdf.pdf \n************************");
            System.out.println(content);
            System.out.println("\nText (horizontal strategy) test pdf.pdf \n************************");
            System.out.println(horizontalContent);
            System.out.println("************************");
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    String extractAndStore(PdfReader reader, String format, RenderFilter... filters) throws Exception
    {
        return extractAndStore(reader, format, LocationTextExtractionStrategy.class, filters);
    }

    <E extends TextExtractionStrategy> String extractAndStore(PdfReader reader, String format, Class<E> strategyClass, RenderFilter... filters) throws Exception
    {
        StringBuilder builder = new StringBuilder();

        for (int page = 1; page <= reader.getNumberOfPages(); page++)
        {
            TextExtractionStrategy strategy = strategyClass.getConstructor().newInstance();
            if (filters != null && filters.length > 0)
            {
                strategy = new FilteredTextRenderListener(strategy, filters);
            }
            String pageText = extract(reader, page, strategy);
            Files.write(Paths.get(String.format(format, page)), pageText.getBytes("UTF8"));

            if (page > 1)
                builder.append("\n\n");
            builder.append(pageText);
        }

        return builder.toString();
    }

    String extract(PdfReader reader, int pageNo, TextExtractionStrategy strategy) throws IOException
    {
        return PdfTextExtractor.getTextFromPage(reader, pageNo, strategy);
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
