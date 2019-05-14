package mkl.testarea.itext5.meta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * <p>
 * This utility compresses PDFs by enforcing the use of (compressed)
 * object streams and cross reference streams.
 * </p>
 * <p>
 * Beware: It does not change the compression of existing streams, it
 * merely forces non-stream indirect objects and cross references into
 * streams which then are compressed.
 * </p>
 * 
 * @author mkl
 */
public class PdfCompressor {

    public static void main(String[] args) throws IOException, DocumentException
    {
        for (String arg: args)
        {
            final File file = new File(arg);
            if (file.exists())
            {
                PdfReader reader = new PdfReader(file.getAbsolutePath());
                FileOutputStream output = new FileOutputStream(new File(file.getParent(), file.getName() + "-compressed.pdf"));
                PdfStamper pdfStamper = new PdfStamper(reader, output);
                pdfStamper.setFullCompression();
                pdfStamper.getWriter().setCompressionLevel(9);
                pdfStamper.close();
            }
            else
                System.err.println("File does not exist: " + file);
        }
    }

}
