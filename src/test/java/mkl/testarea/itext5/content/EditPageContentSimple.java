package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.PRTokeniser;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfContentParser;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfEncodings;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.parser.ContentByteUtils;

/**
 * @author mkl
 */
public class EditPageContentSimple {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/58556591/how-to-replace-text-in-a-pdf-with-correct-encoding-using-itext">
     * How to replace text in a pdf with correct encoding using Itext
     * </a>
     * <br/>
     * <a href="http://www.borlangeenergi.se/Documents/Borl%C3%A4nge%20Energi/Kundservice/Forklaring_av_fakturan.pdf">
     * Forklaring_av_fakturan.pdf
     * </a>
     * <p>
     * This test demonstrates how one can replace text pieces under
     * numerous assumptions, in particular assuming all fonts using
     * an ANSI'ish encoding and bringing along all required glyphs,
     * the text pieces to replace residing in a single text drawing
     * operation string argument each, and all relevant text being
     * drawn immediately in the actual page content.
     * </p>
     */
    @Test
    public void testReplaceInStringArgumentsForklaringAvFakturan() throws IOException, DocumentException {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("Förfallodatum", "Ablaufdatum");
        try (   InputStream resource = getClass().getResourceAsStream("Forklaring_av_fakturan.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "Forklaring_av_fakturan-replaceInStringArguments.pdf"))  ) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            for (int pageNum = 1; pageNum <= pdfReader.getNumberOfPages(); pageNum++) {
                PdfDictionary page = pdfReader.getPageN(pageNum);
                byte[] pageContentInput = ContentByteUtils.getContentBytesForPage(pdfReader, pageNum);
                page.remove(PdfName.CONTENTS);
                replaceInStringArguments(pageContentInput, pdfStamper.getUnderContent(pageNum), replacements);
            }
            pdfStamper.close();
        }
    }

    void replaceInStringArguments(byte[] contentBytesBefore, PdfContentByte canvas, Map<String, String> replacements) throws IOException {
        PRTokeniser tokeniser = new PRTokeniser(new RandomAccessFileOrArray(new RandomAccessSourceFactory().createSource(contentBytesBefore)));
        PdfContentParser ps = new PdfContentParser(tokeniser);
        ArrayList<PdfObject> operands = new ArrayList<PdfObject>();
        while (ps.parse(operands).size() > 0){
            for (int i = 0; i < operands.size(); i++) {
                PdfObject pdfObject = operands.get(i);
                if (pdfObject instanceof PdfString) {
                    operands.set(i, replaceInString((PdfString)pdfObject, replacements));
                } else if (pdfObject instanceof PdfArray) {
                    PdfArray pdfArray = (PdfArray) pdfObject;
                    for (int j = 0; j < pdfArray.size(); j++) {
                        PdfObject arrayObject = pdfArray.getPdfObject(j);
                        if (arrayObject instanceof PdfString) {
                            pdfArray.set(j, replaceInString((PdfString)arrayObject, replacements));
                        }
                    }
                }
            }
            for (PdfObject object : operands)
            {
                object.toPdf(canvas.getPdfWriter(), canvas.getInternalBuffer());
                canvas.getInternalBuffer().append((byte) ' ');
            }
            canvas.getInternalBuffer().append((byte) '\n');
        }
    }

    PdfString replaceInString(PdfString string, Map<String, String> replacements) {
        String value = PdfEncodings.convertToString(string.getBytes(), PdfObject.TEXT_PDFDOCENCODING);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            value = value.replace(entry.getKey(), entry.getValue());
        }
        return new PdfString(PdfEncodings.convertToBytes(value, PdfObject.TEXT_PDFDOCENCODING));
    }
}
