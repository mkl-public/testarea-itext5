package mkl.testarea.itext5.copy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.io.StreamUtil;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class CopyWithField {
    final static File RESULT_FOLDER = new File("target/test-outputs", "copy");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/50832827/concatenate-pdf-without-flattening-but-preserve-fields">
     * Concatenate pdf without flattening but preserve fields
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1sPq_Q7-RIYBRtx3Fuou5eM1_AxvrCGI-">
     * User.pdf
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1EmyQB6ejK4s_HtiayfSfWbUD4m2URDEX">
     * second.pdf
     * </a>
     * <p>
     * The OP's original code was not copying in <i>mergeFields</i> mode. After
     * activating it (and postponing the closing of the source readers after the
     * closing of the target copy), the result PDF has a proper form definition.
     * </p>
     */
    @Test
    public void testCopyLikeLizLamperouge() throws IOException, DocumentException {
        try (   InputStream userResource = getClass().getResourceAsStream("User.pdf");
                InputStream secondResource = getClass().getResourceAsStream("second.pdf")   ) {
            PdfReader reader = new PdfReader(userResource);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfStamper stamper = new PdfStamper(reader, baos);

            PdfWriter writer = stamper.getWriter();
            writer.setPdfVersion(PdfWriter.VERSION_1_7);
            AcroFields form = stamper.getAcroFields();

            form.setField("Name", "Jhon");
            stamper.close();

            List<byte[]> listOfPdfFiles = new ArrayList<>();
            listOfPdfFiles.add(baos.toByteArray());

            byte[] informativaPrivacy = StreamUtil.inputStreamToArray(secondResource);
            listOfPdfFiles.add(informativaPrivacy);

            concatenatePdfs(listOfPdfFiles, new File(RESULT_FOLDER, "merged.pdf"));

            baos.close();
            reader.close();
        }
    }

    void concatenatePdfs(List<byte[]> listOfPdfFiles, File outputFile) throws DocumentException, IOException {
        Document document = new Document();
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        PdfCopy copy = new PdfSmartCopy(document, outputStream);
        copy.setMergeFields(); //<-- added
        document.open();
        List<PdfReader> pdfReaders = new ArrayList<>(); // <-- added
        for (byte[] inFile : listOfPdfFiles) {
            PdfReader reader = new PdfReader(inFile);
            copy.addDocument(reader);
            //reader.close(); <-- removed
            pdfReaders.add(reader); // <-- added
        }
        document.close();
        pdfReaders.forEach(r -> r.close()); // <-- added
    }

}
