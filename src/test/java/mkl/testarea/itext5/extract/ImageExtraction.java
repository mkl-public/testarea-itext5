// $Id$
package mkl.testarea.itext5.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfImageObject.ImageBytesType;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

/**
 * @author mkl
 */
public class ImageExtraction
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/36936524/itextsharp-extracted-cmyk-image-is-inverted">
     * iTextSharp: Extracted CMYK Image is inverted
     * </a>
     * <br/>
     * <a href="http://docdro.id/ZoHmiAd">sampleCMYK.pdf</a>
     * <p>
     * The issue is just the same in iText 5.
     * </p>
     */
    @Test
    public void testExtractCmykImage() throws IOException
    {
        try  (InputStream resourceStream = getClass().getResourceAsStream("sampleCMYK.pdf") )
        {
            PdfReader reader = new PdfReader(resourceStream);
            for (int page = 1; page <= reader.getNumberOfPages(); page++)
            {
                PdfReaderContentParser parser = new PdfReaderContentParser(reader);
                parser.processContent(page, new RenderListener()
                {
                    @Override
                    public void beginTextBlock() { }

                    @Override
                    public void renderText(TextRenderInfo renderInfo) { }

                    @Override
                    public void endTextBlock() { }

                    @Override
                    public void renderImage(ImageRenderInfo renderInfo)
                    {
                        try
                        {
                            byte[] bytes = renderInfo.getImage().getImageAsBytes();
                            ImageBytesType type = renderInfo.getImage().getImageBytesType();
                            if (bytes != null && type != null)
                            {
                                Files.write(new File(RESULT_FOLDER, "sampleCMYK-" + index++ + "." + type.getFileExtension()).toPath(), bytes);
                            }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    
                    int index = 0;
                });

            }
        }        
    }

}
