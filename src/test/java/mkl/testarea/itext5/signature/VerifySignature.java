// $Id$
package mkl.testarea.itext5.signature;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.PdfPKCS7;

/**
 * @author mkl
 */
public class VerifySignature
{
    @BeforeClass
    public static void setUp() throws Exception
    {
        BouncyCastleProvider bcp = new BouncyCastleProvider();
        //Security.addProvider(bcp);
        Security.insertProviderAt(bcp, 1);
    }

    /**
     * <a href="http://stackoverflow.com/questions/35846427/pdfpkcs7-verify-return-false">
     * PdfPKCS7 .verify() return false
     * </a>
     * <br>
     * <a href="http://itext.2136553.n4.nabble.com/PdfPKCS7-verify-return-false-tt4661004.html">
     * PdfPKCS7 .verify() return false
     * </a>
     * <br>
     * <a href="http://itext.2136553.n4.nabble.com/file/n4661004/Test.pdf">
     * TestMGomez.pdf
     * </a>
     * <p>
     * Indeed, iText <code>PdfPKCS7.verify()</code> returns <code>false</code> while Adobe Reader
     * does not complain.
     * </p>
     * <p>
     * The reason for this is that the signature container in the OP's document has a zero-length
     * octet string in the encapsulated content optional eContent. iText assumes this octet string
     * to actually contain a hash (as if the signature were a adbe.pkcs7.sha1 subfilter type).
     * After resetting that value to <code>null</code>, iText also verifies positively.
     * </p>
     */
    @Test
    public void testVerifyTestMGomez() throws IOException, GeneralSecurityException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        try (   InputStream resource = getClass().getResourceAsStream("TestMGomez.pdf") )
        {
            PdfReader reader = new PdfReader(resource);
            AcroFields acroFields = reader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
               System.out.println("Signature name: " + name);
               System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
               PdfPKCS7 pk = acroFields.verifySignature(name);
               System.out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
               System.out.println("Document verifies: " + pk.verify());
            }
        }

        System.out.println();

        Field rsaDataField = PdfPKCS7.class.getDeclaredField("RSAdata");
        rsaDataField.setAccessible(true);
        
        try (   InputStream resource = getClass().getResourceAsStream("TestMGomez.pdf") )
        {
            PdfReader reader = new PdfReader(resource);
            AcroFields acroFields = reader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
               System.out.println("Signature name: " + name);
               System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
               PdfPKCS7 pk = acroFields.verifySignature(name);
               System.out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));

               Object rsaDataFieldContent = rsaDataField.get(pk);
               if (rsaDataFieldContent != null && ((byte[])rsaDataFieldContent).length == 0)
               {
                   System.out.println("Found zero-length encapsulated content: ignoring");
                   rsaDataField.set(pk, null);
               }
               System.out.println("Document verifies: " + pk.verify());
            }
        }
    }
}
