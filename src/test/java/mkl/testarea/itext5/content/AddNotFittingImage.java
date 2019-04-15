package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * <a href="https://stackoverflow.com/questions/55630476/how-to-add-the-words-in-a-new-page-while-adding-images-and-words-in-itext">
 * How to add the words in a new page while adding images and words in itext
 * </a>
 * <p>
 * The tests in this test class analyze how iText behaves when an
 * image is added to a document which does not fit on the current
 * page, and it examines different strategies to tweak this default
 * behavior, using <code>newPage()</code> calls and/or the property
 * <code>StrictImageSequence</code> of {@link PdfWriter}. 
 * </p>
 * 
 * @author mkl
 */
public class AddNotFittingImage {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * iText default behavior: Image is floated to the next page
     * but paragraph drawn after image remains on current page.
     */
    @Test
    public void testAddNotFittingImage() throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "NotFittingImage.pdf")));
        document.open();

        Image image = null;
        try (InputStream imageStream = getClass().getResourceAsStream("2x2colored.png"))
        {
            image = Image.getInstance(IOUtils.toByteArray(imageStream));
            image.scaleAbsolute(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - 1,
                    document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin() - 35);
        }

        document.add(new Paragraph("Some Content"));

        document.add(new Paragraph("Before Image"));

        document.add(image);

        document.add(new Paragraph("After Image"));

        document.close();
    }

    /**
     * iText default behavior with a <code>newPage()</code> call
     * after adding the image: Image is floated to the next page
     * and paragraph drawn after that call comes immediately
     * after the image.
     */
    @Test
    public void testAddNotFittingImageNewPage() throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "NotFittingImage-new-page.pdf")));
        document.open();

        Image image = null;
        try (InputStream imageStream = getClass().getResourceAsStream("2x2colored.png"))
        {
            image = Image.getInstance(IOUtils.toByteArray(imageStream));
            image.scaleAbsolute(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - 1,
                    document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin() - 35);
        }

        document.add(new Paragraph("Some Content"));

        document.add(new Paragraph("Before Image"));

        document.add(image);

        document.newPage();

        document.add(new Paragraph("After Image"));

        document.close();
    }

    /**
     * iText default behavior with 2 <code>newPage()</code> calls
     * after adding the image: Image is floated to the next page
     * and paragraph drawn after that call comes after the image
     * on yet another new page.
     */
    @Test
    public void testAddNotFittingImageDoubleNewPage() throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "NotFittingImage-double-new-page.pdf")));
        document.open();

        Image image = null;
        try (InputStream imageStream = getClass().getResourceAsStream("2x2colored.png"))
        {
            image = Image.getInstance(IOUtils.toByteArray(imageStream));
            image.scaleAbsolute(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - 1,
                    document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin() - 35);
        }

        document.add(new Paragraph("Some Content"));

        document.add(new Paragraph("Before Image"));

        document.add(image);

        document.newPage();
        document.newPage();

        document.add(new Paragraph("After Image"));

        document.close();
    }

    /**
     * iText behavior with <code>StrictImageSequence</code>:
     * A new page is generated on which the image is drawn, the
     * paragraph drawn after image follows the image immediately
     * if possible.
     */
    @Test
    public void testAddNotFittingImageStrictSequence() throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "NotFittingImage-strictSequence.pdf")));
        pdfWriter.setStrictImageSequence(true);
        document.open();

        Image image = null;
        try (InputStream imageStream = getClass().getResourceAsStream("2x2colored.png"))
        {
            image = Image.getInstance(IOUtils.toByteArray(imageStream));
            image.scaleAbsolute(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - 1,
                    document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin() - 35);
        }

        document.add(new Paragraph("Some Content"));

        document.add(new Paragraph("Before Image"));

        document.add(image);

        document.add(new Paragraph("After Image"));

        document.close();
    }

    /**
     * iText behavior with <code>StrictImageSequence</code> with
     * a <code>newPage()</code> call after adding the image:
     * A new page is generated on which the image is drawn, the
     * paragraph drawn after image follows the image on yet
     * another new page.
     */
    @Test
    public void testAddNotFittingImageStrictSequenceNewPage() throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "NotFittingImage-strictSequence-newPage.pdf")));
        pdfWriter.setStrictImageSequence(true);
        document.open();

        Image image = null;
        try (InputStream imageStream = getClass().getResourceAsStream("2x2colored.png"))
        {
            image = Image.getInstance(IOUtils.toByteArray(imageStream));
            image.scaleAbsolute(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - 1,
                    document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin() - 35);
        }

        document.add(new Paragraph("Some Content"));

        document.add(new Paragraph("Before Image"));

        document.add(image);

        document.newPage();

        document.add(new Paragraph("After Image"));

        document.close();
    }
}
