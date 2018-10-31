package mkl.testarea.itext5.copy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class CopyWithResizeRotate {
    final static File RESULT_FOLDER = new File("target/test-outputs", "copy");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/53016204/copying-annotations-with-pdfwriter-instead-of-pdfcopy">
     * Copying annotations with PdfWriter instead of PdfCopy
     * </a>
     * <p>
     * This test shows how to resize a document while
     * copying it.
     * </p>
     */
    @Test
    public void testResizeAndCopy() throws IOException, DocumentException {
        byte[] wildPdf = createWildPdf(pageSizes);
        Files.write(new File(RESULT_FOLDER, "wild.pdf").toPath(), wildPdf);

        PdfReader pdfReaderToLegal = new PdfReader(wildPdf);
        resize(pdfReaderToLegal, PageSize.LEGAL.getWidth(), PageSize.LEGAL.getHeight());
        PdfReader pdfReaderToLetter = new PdfReader(wildPdf);
        resize(pdfReaderToLetter, PageSize.LETTER.getWidth(), PageSize.LETTER.getHeight());

        try (   OutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "wild-resized.pdf"))) {
            Document document = new Document();
            PdfCopy pdfCopy = new PdfCopy(document, os);
            document.open();
            pdfCopy.addDocument(pdfReaderToLetter);
            pdfCopy.addDocument(pdfReaderToLegal);
            document.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/53016204/copying-annotations-with-pdfwriter-instead-of-pdfcopy">
     * Copying annotations with PdfWriter instead of PdfCopy
     * </a>
     * <p>
     * This test shows how to rotate and/or resize a document while
     * copying it.
     * </p>
     */
    @Test
    public void testRotateResizeAndCopy() throws IOException, DocumentException {
        byte[] wildPdf = createWildPdf(pageSizes);
        Files.write(new File(RESULT_FOLDER, "wild.pdf").toPath(), wildPdf);

        PdfReader pdfReaderOriginal = new PdfReader(wildPdf);
        PdfReader pdfReaderRotate = new PdfReader(wildPdf);
        rotate(pdfReaderRotate);
        PdfReader pdfReaderResize = new PdfReader(wildPdf);
        resize(pdfReaderResize, PageSize.LETTER.getWidth(), PageSize.LETTER.getHeight());
        PdfReader pdfReaderRotateResize = new PdfReader(wildPdf);
        rotate(pdfReaderRotateResize);
        resize(pdfReaderRotateResize, PageSize.LETTER.getWidth(), PageSize.LETTER.getHeight());

        try (   OutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "wild-rotated-resized.pdf"))) {
            Document document = new Document();
            PdfCopy pdfCopy = new PdfCopy(document, os);
            document.open();
            pdfCopy.addDocument(pdfReaderOriginal);
            pdfCopy.addDocument(pdfReaderRotate);
            pdfCopy.addDocument(pdfReaderResize);
            pdfCopy.addDocument(pdfReaderRotateResize);
            document.close();
        }
    }

    /**
     * Resizes a document in a {@link PdfReader} to the given dimensions.
     * 
     * @see #testResizeAndCopy()
     * @see #testRotateResizeAndCopy()
     */
    void resize(PdfReader pdfReader, float width, float height) {
        for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
            boolean switched = pdfReader.getPageRotation(i) % 180 != 0;
            float widthHere = switched ? height : width;
            float heightHere = switched ? width : height;

            Rectangle cropBox = pdfReader.getCropBox(i);
            float halfWidthGain = (widthHere - cropBox.getWidth()) / 2;
            float halfHeightGain = (heightHere - cropBox.getHeight()) / 2;
            Rectangle newCropBox = new Rectangle(cropBox.getLeft() - halfWidthGain, cropBox.getBottom() - halfHeightGain,
                    cropBox.getRight() + halfWidthGain, cropBox.getTop() + halfHeightGain);

            Rectangle mediaBox = pdfReader.getPageSize(i);
            Rectangle newMediaBox = new Rectangle(Math.min(newCropBox.getLeft(), mediaBox.getLeft()),
                    Math.min(newCropBox.getBottom(), mediaBox.getBottom()),
                    Math.max(newCropBox.getRight(), mediaBox.getRight()),
                    Math.max(newCropBox.getTop(), mediaBox.getTop()));

            PdfDictionary pageDictionary = pdfReader.getPageN(i);
            pageDictionary.put(PdfName.MEDIABOX, new PdfArray(new float[] {newMediaBox.getLeft(), newMediaBox.getBottom(),
                    newMediaBox.getRight(), newMediaBox.getTop()}));
            pageDictionary.put(PdfName.CROPBOX, new PdfArray(new float[] {newCropBox.getLeft(), newCropBox.getBottom(),
                    newCropBox.getRight(), newCropBox.getTop()}));
        }
    }

    /**
     * Rotates a document in a {@link PdfReader}.
     * 
     * @see #testRotateResizeAndCopy()
     */
    void rotate(PdfReader pdfReader) {
        for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
            int rotation = pdfReader.getPageRotation(i);
            int newRotation = rotation + 90 % 360;

            PdfDictionary pageDictionary = pdfReader.getPageN(i);
            if (newRotation == 0)
                pageDictionary.remove(PdfName.ROTATE);
            else
                pageDictionary.put(PdfName.ROTATE, new PdfNumber(newRotation));
        }
    }

    /**
     * Creates a Lorem Ipsum file with pages using the given sizes.
     * 
     * @see #testResizeAndCopy()
     * @see #testRotateResizeAndCopy()
     */
    byte[] createWildPdf(Rectangle[] pageSizes) throws IOException, DocumentException {
        try (   ByteArrayOutputStream baos = new ByteArrayOutputStream();   ) {
            Document document = new Document(pageSizes[pageSizes.length - 1]);
            PdfWriter pdfWriter = PdfWriter.getInstance(document, baos);
            PageSizeSwitcher pageSizeSwitcher = new PageSizeSwitcher(document, pageSizes);
            pdfWriter.setPageEvent(pageSizeSwitcher);
            document.open();
            do {
                document.add(new Paragraph("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."));
            } while (!pageSizeSwitcher.finished);
            document.close();
            return baos.toByteArray();
        }
    }

    /**
     * A selection of page sizes for creating a wild PDF using {@link #createWildPdf(Rectangle[])}.
     * 
     * @see #testResizeAndCopy()
     * @see #testRotateResizeAndCopy()
     */
    static Rectangle[] pageSizes = new Rectangle[] {
            PageSize.HALFLETTER,
            PageSize.A5,
            PageSize.A5.rotate(),
            new Rectangle(500, 500, 1000, 1200),
            PageSize.A4,
    };

    /**
     * Helper class for {@link CopyWithResizeRotate#createWildPdf(Rectangle[])}.
     */
    class PageSizeSwitcher extends PdfPageEventHelper {
        PageSizeSwitcher(Document document, Rectangle[] pageSizes) {
            this.document = document;
            this.pageSizes = pageSizes;
            this.sizeIndex = 0;
        }

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            super.onStartPage(writer, document);
            this.document.setPageSize(pageSizes[sizeIndex]);
            if (++sizeIndex >= pageSizes.length) {
                sizeIndex = 0;
                finished = true;
            }
        }

        final Document document;
        final Rectangle[] pageSizes;
        int sizeIndex;
        boolean finished = false;
    }
}
