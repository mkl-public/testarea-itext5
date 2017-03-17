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
        System.out.println("\n\nTestMGomez.pdf\n==============");
    	
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

    /**
     * <a href="http://stackoverflow.com/questions/37726215/why-does-my-signature-revision-number-increment-by-2-in-itext-after-detached-s">
     * Why does my signature revision number increment by 2 (in itext) after detached signing?
     * </a>
     * <br/>
     * <a href="https://onedrive.live.com/redir?resid=2F03BFDA84B77A41!113&authkey=!ABPGZ7pxuxoE8A0&ithint=file%2Cpdf">
     * signedoutput.pdf
     * </a>
     * <p>
     * The issue cannot be reproduced. In particular the PDF contains only a single revision.
     * </p>
     */
    @Test
    public void testVerifySignedOutput() throws IOException, GeneralSecurityException
    {
        System.out.println("\n\nsignedoutput.pdf\n================");
    	
        try (   InputStream resource = getClass().getResourceAsStream("signedoutput.pdf") )
        {
            PdfReader reader = new PdfReader(resource);
            AcroFields acroFields = reader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
               System.out.println("Signature name: " + name);
               System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
               System.out.println("Document revision: " + acroFields.getRevision(name) + " of " + acroFields.getTotalRevisions());
               PdfPKCS7 pk = acroFields.verifySignature(name);
               System.out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
               System.out.println("Document verifies: " + pk.verify());
            }
        }

        System.out.println();
    }

    /**
     * <a href="http://stackoverflow.com/questions/42824577/itext-can-not-verify-signed-pdf-docs-edited-by-nitro-pro-10-11">
     * itext can not verify signed pdf docs edited by nitro pro 10/11
     * </a>
     * <br/>
     * <a href="https://alimail.fadada.com/signed.pdf">
     * babylove_signed.pdf
     * </a>
     * <p>
     * Validation correctly shows verification success for a single
     * signature that does cover the whole document.
     * </p>
     */
    @Test
    public void testVerifyBabyloveSigned() throws IOException, GeneralSecurityException
    {
        System.out.println("\n\nbabylove_signed.pdf\n===================");
    	
        try (   InputStream resource = getClass().getResourceAsStream("babylove_signed.pdf") )
        {
            PdfReader reader = new PdfReader(resource);
            AcroFields acroFields = reader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
               System.out.println("Signature name: " + name);
               System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
               System.out.println("Document revision: " + acroFields.getRevision(name) + " of " + acroFields.getTotalRevisions());
               PdfPKCS7 pk = acroFields.verifySignature(name);
               System.out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
               System.out.println("Document verifies: " + pk.verify());
            }
        }

        System.out.println();
    }
    
    /**
     * <a href="http://stackoverflow.com/questions/42824577/itext-can-not-verify-signed-pdf-docs-edited-by-nitro-pro-10-11">
     * itext can not verify signed pdf docs edited by nitro pro 10/11
     * </a>
     * <br/>
     * <a href="https://alimail.fadada.com/signed&modify_by_nitro.pdf">
     * babylove_signed&modify_by_nitro.pdf
     * </a>
     * <p>
     * Validation correctly shows verification success for a single
     * signature that does <b>not</b> cover the whole document.
     * </p>
     */
    @Test
    public void testVerifyBabyloveSignedAndModifyByNitro() throws IOException, GeneralSecurityException
    {
        System.out.println("\n\nbabylove_signed&modify_by_nitro.pdf\n===================");
    	
        try (   InputStream resource = getClass().getResourceAsStream("babylove_signed&modify_by_nitro.pdf") )
        {
            PdfReader reader = new PdfReader(resource);
            AcroFields acroFields = reader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
               System.out.println("Signature name: " + name);
               System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
               System.out.println("Document revision: " + acroFields.getRevision(name) + " of " + acroFields.getTotalRevisions());
               PdfPKCS7 pk = acroFields.verifySignature(name);
               System.out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
               System.out.println("Document verifies: " + pk.verify());
            }
        }

        System.out.println();
    }
}
