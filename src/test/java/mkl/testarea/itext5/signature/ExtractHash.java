/**
 * 
 */
package mkl.testarea.itext5.signature;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.HexEncoder;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;

/**
 * <a href="http://stackoverflow.com/questions/29939831/obtaining-the-hash-digest-from-a-pcks7-signed-pdf-file-with-itext">
 * Obtaining the hash/digest from a PCKS7 signed PDF file with iText
 * </a>
 * <p>
 * {@link #extractHashes(PdfReader, String)} implements a sample routine extracting the message digest.
 * </p>
 * 
 * @author mkl
 */
public class ExtractHash
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "signature");

    @BeforeClass
    public static void setUp() throws Exception
    {
        RESULT_FOLDER.mkdirs();

        BouncyCastleProvider bcp = new BouncyCastleProvider();
        //Security.addProvider(bcp);
        Security.insertProviderAt(bcp, 1);
    }

    @Test
    public void testFirstPage11P0022AD_20150202164018_307494() throws IOException, GeneralSecurityException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        try (   InputStream resource = getClass().getResourceAsStream("FirstPage11P0022AD_20150202164018_307494.pdf")   )
        {
            System.out.println("FirstPage11P0022AD_20150202164018_307494.pdf");
            PdfReader reader = new PdfReader(resource);
            extractHashes(reader, "FirstPage11P0022AD_20150202164018_307494-%s.hash");
        }
    }

    void extractHashes(PdfReader reader, String format) throws NoSuchFieldException, SecurityException, GeneralSecurityException, IllegalArgumentException, IllegalAccessException, IOException
    {
        AcroFields acroFields = reader.getAcroFields();
        List<String> names = acroFields.getSignatureNames();

        for (String name: names)
        {
            System.out.printf("  %s\n", name);
            PdfPKCS7 pdfPkcs7 = acroFields.verifySignature(name);
            System.out.printf("    Digest algorithm: %s\n", pdfPkcs7.getHashAlgorithm());
            pdfPkcs7.verify();

            Field digestAttrField = PdfPKCS7.class.getDeclaredField("digestAttr");
            digestAttrField.setAccessible(true);
            byte[] digestAttr = (byte[]) digestAttrField.get(pdfPkcs7);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            new HexEncoder().encode(digestAttr, 0, digestAttr.length, baos);
            byte[] digestAttrHex = baos.toByteArray();
            System.out.printf("    Hash: %s\n", new String(digestAttrHex));

            Files.write(new File(RESULT_FOLDER, String.format(format, name)).toPath(), digestAttr);
            Files.write(new File(RESULT_FOLDER, String.format(format, name) + ".hex").toPath(), digestAttrHex);
        }
    }
}
