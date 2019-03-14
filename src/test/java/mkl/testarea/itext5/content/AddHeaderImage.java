package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class AddHeaderImage
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/35035356/itext-add-an-image-on-a-header-with-an-absolute-position">
     * IText : Add an image on a header with an absolute position
     * </a>
     * <p>
     * This test demonstrates how to add an image a a fixed position on a page.
     * </p>
     */
    @Test
    public void testAddHeaderImageFixed() throws IOException, DocumentException
    {
        try (   FileOutputStream stream = new FileOutputStream(new File(RESULT_FOLDER, "headerImage.pdf"))    )
        {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, stream);
            writer.setPageEvent(new PdfPageEventHelper()
            {
                Image imgSoc = null;

                @Override
                public void onOpenDocument(PdfWriter writer, Document document)
                {
                    try (InputStream imageStream = getClass().getResourceAsStream("/mkl/testarea/itext5/layer/Willi-1.jpg "))
                    {
                        imgSoc = Image.getInstance(IOUtils.toByteArray(imageStream));
                        imgSoc.scaleToFit(110,110);
                        imgSoc.setAbsolutePosition(390, 720);
                    }
                    catch (BadElementException | IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    super.onOpenDocument(writer, document);
                }

                @Override
                public void onEndPage(PdfWriter writer, Document document)
                {
                    try
                    {
                        writer.getDirectContent().addImage(imgSoc);
                    }
                    catch (DocumentException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
            document.open();
            
            document.add(new Paragraph("PAGE 1"));
            document.newPage();
            document.add(new Paragraph("PAGE 2"));
            
            document.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/54949863/itext-adding-image-in-header-empy-chunk">
     * Itext - Adding image in header - Empy Chunk
     * </a>
     * <p>
     * Indeed, image chunks get scaled down to 3 units by
     * {@link ColumnText#showTextAligned(com.itextpdf.text.pdf.PdfContentByte, int, Phrase, float, float, float)}
     * or alternatively get dropped altogether, depending on image flags.
     * The solution is to instantiate and use {@link ColumnText} oneself
     * with a sensible available column height, see below in
     * {@link PDFHeaderFooter#onEndPage(PdfWriter, Document)}.
     * </p>
     */
    @Test
    public void testAddHeaderImageLikeBesmart() throws IOException, DocumentException {
        try (   OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "created-with-header-image.pdf"))   ) {
            Document document = new Document();
            PdfWriter pdfWriter = PdfWriter.getInstance(document, result);
            pdfWriter.setPageEvent(new PDFHeaderFooter());
            document.open();
            for (int i=0; i < 20; i++) {
                document.add(new Paragraph("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."));
            }
            document.close();
        }
    }

    /**
     * @see AddHeaderImage#testAddHeaderImageLikeBesmart()
     */
    class PDFHeaderFooter extends PdfPageEventHelper {
        public PDFHeaderFooter() throws MalformedURLException, IOException, DocumentException {
            super();
        }

        Image image = Image.getInstance("src/test/resources/mkl/testarea/itext5/layer/Willi-1.jpg");
        Phrase header = new Phrase(new Chunk(image, 0, 0, true));
        int pagenumber;

        public void onChapter(PdfWriter writer, Document document, float paragraphPosition, Paragraph title) {
            pagenumber = 1;
        }

        public void onStartPage(PdfWriter writer, Document document) {
            pagenumber++;
        }

        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle rect = document.getPageSize();// writer.getBoxSize("art");
            System.out.println(header.getContent());

            /*
             *  Replace
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, header, rect.getRight(),
                    rect.getTop()-30, 0);
             *  by
             */

            ColumnText ct = new ColumnText(writer.getDirectContent());
            ct.setSimpleColumn(header, rect.getLeft(), rect.getTop(30), rect.getRight(), rect.getTop(), 2, Element.ALIGN_RIGHT);
            try {
                ct.go();
            } catch (DocumentException e) {
                throw new ExceptionConverter(e);
            }

            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT,
                    new Phrase(String.format("pag. %d", pagenumber)), (rect.getLeft() + rect.getRight()) / 2,
                    rect.getBottom() - 18, 0);
        }
    }
}
