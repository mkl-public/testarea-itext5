package mkl.testarea.itext5.form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class EditXfaFormData {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/57485614/itextsharp-with-signed-file">
     * iTextsharp with signed file
     * </a>
     * <br/>
     * <a href="https://1drv.ms/b/s!AnggmeMAJPFggYRnU5nSWmKCc8qbkA?e=LvoWe9">
     * prueba_xfa.pdf
     * </a>
     * <p>
     * Cannot reproduce what the OP claims, both the Java and the
     * C# version leave the second signature unbroken.
     * </p>
     */
    @Test
    public void testEditXfaLikeWalterB() throws IOException, DocumentException {
        PdfReader pdfReader = null;
        PdfStamper stamper = null;

        try (   InputStream inStream = getClass().getResourceAsStream("prueba_xfa.pdf"))
        {
            pdfReader = new PdfReader(inStream);
        }

        try (   OutputStream outStream = new FileOutputStream(new File(RESULT_FOLDER, "prueba_xfa_out.pdf")))
        {
            stamper = new PdfStamper(pdfReader, outStream, '\0', true);

            AcroFields form = stamper.getAcroFields();

            form.setField("FORMULARIO[0].SUBFORMULARIO[0].ConsejoSubForm[0].OBLEA[0]", "probando");

            stamper.close();
            pdfReader.close();
        }
    }
}
