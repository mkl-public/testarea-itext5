package mkl.testarea.itext5.form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * This test covers situations with multiple widgets of the same field.
 * 
 * @author mkl
 */
public class SameFieldTwice
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/28943245/possible-to-ues-variables-in-a-pdf-doument">
     * Possible to ues variables in a PDF doument?
     * </a>
     * <p>
     * Generates a sample PDF containing two widgets of the same text field.
     * </p>
     */
    @Test
    public void testCreateFormWithSameFieldTwice() throws IOException, DocumentException
    {
        try (   OutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "aFieldTwice.pdf"))  )
        {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, os);
            document.open();
            document.add(new Paragraph("The same field twice"));
            
            PdfFormField field = PdfFormField.createTextField(writer, false, false, 0);
            field.setFieldName("fieldName");

            PdfFormField annot1 = PdfFormField.createEmpty(writer);
            annot1.setWidget(new Rectangle(30, 700, 200, 720), PdfAnnotation.HIGHLIGHT_INVERT);
            field.addKid(annot1);

            PdfFormField annot2 = PdfFormField.createEmpty(writer);
            annot2.setWidget(new Rectangle(230, 700, 400, 720), PdfAnnotation.HIGHLIGHT_INVERT);
            field.addKid(annot2);

            writer.addAnnotation(field);
            
            document.close();
        }
    }
}
