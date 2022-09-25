package mkl.testarea.itext5.extract;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * A simple tool to extract a single page of a PDF.
 * 
 * @author mkl
 */
public class PageExtractor {

    public static void main(String[] args) throws IOException, DocumentException {
        if (args.length < 2) {
            System.err.println("Usage: PageExtractor pageNumber PDF");
        } else {
            PdfReader.unethicalreading = true;
            PdfReader pdfReader = new PdfReader(args[1]);
            pdfReader.selectPages(Collections.singletonList(Integer.decode(args[0])));
            PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(args[1] + "-" + args[0] + ".pdf"));
            pdfStamper.setFullCompression();
            pdfStamper.close();
        }
    }
}
