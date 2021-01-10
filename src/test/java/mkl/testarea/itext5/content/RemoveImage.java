package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Annotation;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.io.StreamUtil;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;

public class RemoveImage {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/65381400/remove-or-update-added-image-icon-from-pdf-page-using-openpdf-based-on-itext-cor">
     * Remove or Update added Image icon from pdf page using OpenPdf based on iText Core
     * </a>
     * <br/>
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/464">
     * Remove or clear added icon images from PDF file
     * </a>
     * <p>
     * This test shows how to remove (or more exactly: replace by something
     * invisible) all images (and form Xobjects) on a page. Judging by a post
     * of the OP on github, this should suffice.
     * </p>
     */
    @Test
    public void testRemoveImageAddedLikeHamidReza() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                InputStream imageResource = getClass().getResourceAsStream("2x2colored.png");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "test-withImageToRemove.pdf"))) {
            Image image = Image.getInstance(StreamUtil.inputStreamToArray(imageResource));
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            addImageLikeHamidReza(pdfStamper, 1, image, 100, 500);
            pdfStamper.close();
            pdfReader.close();
        }

        try (   InputStream input = new FileInputStream(new File(RESULT_FOLDER, "test-withImageToRemove.pdf"));
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "test-withImageRemoved.pdf"))   ) {
            PdfReader pdfReader = new PdfReader(input);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);

            // get page
            PdfDictionary page = pdfReader.getPageN(1);
            PdfDictionary resources = page.getAsDict(PdfName.RESOURCES);

            // get page resources
            PdfArray annots = page.getAsArray(PdfName.ANNOTS);
            PdfDictionary xobjects = resources.getAsDict(PdfName.XOBJECT);

            PdfTemplate pdfTemplate = PdfTemplate.createTemplate(pdfStamper.getWriter(), 1, 1);

            // remove Xobjects
            for (PdfName key : xobjects.getKeys()) {
                xobjects.put(key, pdfTemplate.getIndirectReference());
            }

            // remove annots
            annots.getArrayList().clear();

            pdfStamper.close();
            pdfReader.close();
        }
    }

    /** @see #testRemoveImageAddedLikeHamidReza() */
    void addImageLikeHamidReza(PdfStamper stamp, int pageIndex, Image img, float x, float y) throws DocumentException {
        PdfContentByte over;

        img.setAnnotation(new Annotation(0, 0, 0, 0, "https://github.com/LibrePDF/OpenPDF"));
        img.setAbsolutePosition(x, y);
        img.scaleAbsolute(50, 50);

        // annotation added into target page
        over = stamp.getOverContent(pageIndex);

        over.addImage(img);
    }
}
