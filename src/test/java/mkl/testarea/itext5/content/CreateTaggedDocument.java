package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfStructureElement;
import com.itextpdf.text.pdf.PdfStructureTreeRoot;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class CreateTaggedDocument {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/61517914/how-to-tagged-pdf-with-low-level-object-of-itextpdf-5-5-13">
     * How to Tagged PDF with low level object of itextpdf-5.5.13
     * </a>
     * <p>
     * Doing tagging manually here one also has to do other tasks
     * otherwise done under the hood, in this case adding structure
     * element indirect objects to the PDF.
     * </p>
     */
    @Test
    public void testCreateLowLevelTagged() throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "lowLevelTagged.pdf")));
        writer.setTagged();
        document.open();
        PdfStructureTreeRoot structureTreeRoot = writer.getStructureTreeRoot();
        PdfStructureElement top = new PdfStructureElement(structureTreeRoot, PdfName.DOCUMENT);
        PdfStructureElement element = new PdfStructureElement(top, PdfName.P);
        PdfContentByte cb = writer.getDirectContent();
        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
        cb.setLeading(16);
        cb.setFontAndSize(bf, 12);
        cb.beginText();
        cb.setTextMatrix(50, 700);
        cb.beginMarkedContentSequence(element);
        cb.newlineShowText("Hello There");
        cb.endMarkedContentSequence();
        cb.endText();
        // vvv--- add structure elements to PDF
        writer.addToBody(element, element.getReference());
        writer.addToBody(top, top.getReference());
        // ^^^--- add structure elements to PDF
        document.close();
    }

}
