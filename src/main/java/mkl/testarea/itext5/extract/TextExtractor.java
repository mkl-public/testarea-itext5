package mkl.testarea.itext5.extract;

import java.io.IOException;
import java.io.PrintWriter;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

/**
 * A simple text extractor utility to check what text iText extracts
 * from given PDFs.
 * 
 * @author mkl
 */
public class TextExtractor {
    public static void main(String[] args) throws IOException {
        for (String arg : args) {
            System.out.printf("\n***********\n* %s\n**********\n", arg);
            PdfReader reader = new PdfReader(arg);
            try (PrintWriter printWriter = new PrintWriter(arg + ".txt")) {
                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    String text = PdfTextExtractor.getTextFromPage(reader, i);
                    if (i < 5) {
                        System.out.printf("*\n* page %d\n*\n%s\n", i, text);
                    } else {
                        System.out.print('.');
                    }
                    printWriter.printf("*\n* page %d\n*\n%s\n", i, text);
                }
            }
        }
    }
}
