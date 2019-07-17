package mkl.testarea.itext5.xmlworker;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

/**
 * @author mkl
 */
public class TimingXmlWorker {
    final static File RESULT_FOLDER = new File("target/test-outputs", "xmlworker");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/57043863/is-there-an-efficient-way-to-write-several-html-strings-to-a-pdf-document-in-jav">
     * Is there an efficient way to write several HTML strings to a PDF document in Java?
     * </a>
     * <p>
     * The time this takes is clearly lower than the time observed
     * by the OP. The difference might be explainable by very bad
     * equipment. But see {@link #testMakePdfLikeEvanVSingleWorkerCall()}.
     * </p>
     */
    @Test
    public void testMakePdfLikeEvanV() throws IOException, DocumentException {
        File filename = new File(RESULT_FOLDER, "MakePdfLikeEvanV.pdf");
        int fieldCount = 35;

        long start = System.nanoTime();

        OutputStream file = new FileOutputStream(filename);
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, file);
        document.open();
//        List<FieldValue> values = tc.getFieldValues();
        for (int i = 0; i < /*values.size()*/fieldCount; ++i) {
//            FieldValue fv = values.get(i);
            InputStream is = new ByteArrayInputStream(/*fv.getValue()*/("<p>" + "Value " + i + "</p>").getBytes());
            XMLWorkerHelper.getInstance().parseXHtml(writer, document, is);
            document.add(new Paragraph("\n"));
        }
        document.close();

        long end = System.nanoTime();
        System.out.printf("Created %s with %d fields in %f seconds.\n", filename.getName(), fieldCount, ((float)end - (float)start) / 1000000000f);
    }

    /**
     * <a href="https://stackoverflow.com/questions/57043863/is-there-an-efficient-way-to-write-several-html-strings-to-a-pdf-document-in-jav">
     * Is there an efficient way to write several HTML strings to a PDF document in Java?
     * </a>
     * <p>
     * In contrast to {@link #testMakePdfLikeEvanV()} in this test
     * the HTML pieces are glued together and then transformed using
     * only a single XMLWorker call.
     * </p>
     * <p>
     * I had to drastically increase the number of "fields" to
     * get a runtime above one second! Thus the observation by the
     * OP that this change doesn't improve the runtime cannot be
     * explained by bad equipment anymore, there must be an unknown
     * factor acting as a break.
     * </p>
     */
    @Test
    public void testMakePdfLikeEvanVSingleWorkerCall() throws IOException, DocumentException {
        File filename = new File(RESULT_FOLDER, "MakePdfLikeEvanVSingleWorkerCall.pdf");
        int fieldCount = 10000;

        long start = System.nanoTime();

        OutputStream file = new FileOutputStream(filename);
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, file);
        document.open();
        StringBuilder xmlString = new StringBuilder();
        for (int i = 0; i < fieldCount; ++i) {
            xmlString.append("<p>")
                     .append(("Value " + i))
                     .append("</p>");
        }
        InputStream is = new ByteArrayInputStream(xmlString.toString().getBytes());
        XMLWorkerHelper.getInstance().parseXHtml(writer, document, is);
        document.close();

        long end = System.nanoTime();
        System.out.printf("Created %s with %d fields in %f seconds.\n", filename.getName(), fieldCount, ((float)end - (float)start) / 1000000000f);
    }
}
