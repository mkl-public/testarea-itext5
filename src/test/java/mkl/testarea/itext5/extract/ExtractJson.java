package mkl.testarea.itext5.extract;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.io.StreamUtil;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.TextField;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

/**
 * @author mkl
 */
public class ExtractJson {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/60089111/error-while-reading-json-from-pdf-file-using-itext">
     * Error while reading json from pdf file using iText
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1ShI1CrPvktTm7us0UJ2sP0qsOJaXMU0-a-ONrfSAWcY">
     * Valid Json.txt
     * </a>
     * <p>
     * Indeed, rendering the json to PDF introduced extra line breaks. Some of them don't
     * change the represented object, in particular extra line breaks outside names or
     * values.
     * </p>
     * <p>
     * Line breaks inside names or values are a different story, though. If one assumes
     * that there shouldn't be any line breaks inside them, one can simply remove them.
     * But if iText broke a line at a space, that space is dropped, and as it is not clear
     * in the result whether such a space was dropped or not at a line break, faithful
     * extraction in general is not possible.
     * </p>
     * <p>
     * Thus, one has to change the way one embeds the json data.
     * </p>
     * @see #testJsonToPdfToJsonFormField()
     */
    @Test
    public void testJsonToPdfToJsonLikeHitendra() throws IOException, DocumentException {
        String originalJson = null;
        try (   InputStream resource = getClass().getResourceAsStream("Valid Json.txt")) {
            byte[] resourceBytes = StreamUtil.inputStreamToArray(resource);
            originalJson = new String(resourceBytes);
        }

        File jsonPdfFile = new File(RESULT_FOLDER, "JsonLikeHitendra.pdf");
        File extractedJsonFile = new File(RESULT_FOLDER, "JsonLikeHitendra.txt");

        Document document = new Document();
        document.setPageSize(PageSize.A4);
        document.addCreationDate();
        document.addAuthor("Me");
        PdfWriter.getInstance(document, new FileOutputStream(jsonPdfFile));
        document.open();
        document.add(new Paragraph(originalJson));
        document.close();

        PdfReader pdfReader = new PdfReader(jsonPdfFile.getAbsolutePath());
        int numberOfPages = pdfReader.getNumberOfPages();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= numberOfPages; i++) {
            stringBuilder.append(PdfTextExtractor.getTextFromPage(pdfReader, i));
        }
        pdfReader.close();
        String jsonBody = stringBuilder.toString();

        Files.write(extractedJsonFile.toPath(), Collections.singleton(jsonBody));
    }

    /**
     * <a href="https://stackoverflow.com/questions/60089111/error-while-reading-json-from-pdf-file-using-itext">
     * Error while reading json from pdf file using iText
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1ShI1CrPvktTm7us0UJ2sP0qsOJaXMU0-a-ONrfSAWcY">
     * Valid Json.txt
     * </a>
     * <p>
     * Adding the content in a form field instead of in the static text content allows
     * faithful extraction of the json object.
     * </p>
     * @see #testJsonToPdfToJsonLikeHitendra()
     */
    @Test
    public void testJsonToPdfToJsonFormField() throws IOException, DocumentException {
        String originalJson = null;
        try (   InputStream resource = getClass().getResourceAsStream("Valid Json.txt")) {
            byte[] resourceBytes = StreamUtil.inputStreamToArray(resource);
            originalJson = new String(resourceBytes);
        }

        File jsonPdfFile = new File(RESULT_FOLDER, "JsonFormField.pdf");
        File extractedJsonFile = new File(RESULT_FOLDER, "JsonFormField.txt");

        Document document = new Document();
        document.setPageSize(PageSize.A4);
        document.addCreationDate();
        document.addAuthor("Me");
        PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(jsonPdfFile));
        document.open();
        pdfWriter.getAcroForm().setNeedAppearances(true);
        TextField textField = new TextField(pdfWriter, document.getPageSize(), "json");
        textField.setOptions(TextField.MULTILINE | TextField.READ_ONLY);
        PdfFormField field = textField.getTextField();
        field.setValueAsString(originalJson);
        pdfWriter.addAnnotation(field);
        document.close();

        PdfReader pdfReader = new PdfReader(jsonPdfFile.getAbsolutePath());
        String jsonBody = pdfReader.getAcroFields().getField("json");
        pdfReader.close();

        Files.write(extractedJsonFile.toPath(), Collections.singleton(jsonBody));
    }
}
