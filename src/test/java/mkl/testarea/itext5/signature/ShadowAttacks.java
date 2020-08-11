package mkl.testarea.itext5.signature;

import java.io.IOException;
import java.io.InputStream;
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
 * <p>
 * This test class checks how iText 5 validation reacts to the example files
 * for the "shadow attacks" provided by Ruhr Uni Bochum PDF insecurity site
 * https://www.pdf-insecurity.org/signature-shadow/shadow-attacks.html
 * </p>
 * <p>
 * As it turns out (and as was to be expected by the description of the
 * attack), iText recognizes in all cases that the signature does not
 * cover the whole document which implies that (allowed or disallowed)
 * changes have been made.
 * </p>
 * 
 * @author mkl
 */
public class ShadowAttacks {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        BouncyCastleProvider bcp = new BouncyCastleProvider();
        Security.insertProviderAt(bcp, 1);
    }

    @Test
    public void testVerifyHideShadowFileSigned() throws IOException, GeneralSecurityException {
        System.out.println("\n\nhide-shadow-file-signed.pdf\n======");
        try (   InputStream resource = getClass().getResourceAsStream("hide-shadow-file-signed.pdf")    ) {
            PdfReader pdfReader = new PdfReader(resource);
            AcroFields acroFields = pdfReader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
                System.out.println("===== " + name + " =====");
                System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
                System.out.println("Document revision: " + acroFields.getRevision(name) + " of " + acroFields.getTotalRevisions());
                PdfPKCS7 pkcs7 = acroFields.verifySignature(name);
                System.out.println("Subject: " + CertificateInfo.getSubjectFields(pkcs7.getSigningCertificate()));
                System.out.println("Integrity check OK? " + pkcs7.verify());
            }
            System.out.println();
        }
    }

    @Test
    public void testVerifyreplaceShadowFileSignedManipulated() throws IOException, GeneralSecurityException {
        System.out.println("\n\nreplace-shadow-file-signed-manipulated.pdf\n======");
        try (   InputStream resource = getClass().getResourceAsStream("replace-shadow-file-signed-manipulated.pdf")    ) {
            PdfReader pdfReader = new PdfReader(resource);
            AcroFields acroFields = pdfReader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
                System.out.println("===== " + name + " =====");
                System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
                System.out.println("Document revision: " + acroFields.getRevision(name) + " of " + acroFields.getTotalRevisions());
                PdfPKCS7 pkcs7 = acroFields.verifySignature(name);
                System.out.println("Subject: " + CertificateInfo.getSubjectFields(pkcs7.getSigningCertificate()));
                System.out.println("Integrity check OK? " + pkcs7.verify());
            }
            System.out.println();
        }
    }

    @Test
    public void testVerifyHideAndReplaceShadowFileSigned1() throws IOException, GeneralSecurityException {
        System.out.println("\n\nhide-and-replace-shadow-file-signed-1.pdf\n======");
        try (   InputStream resource = getClass().getResourceAsStream("hide-and-replace-shadow-file-signed-1.pdf")    ) {
            PdfReader pdfReader = new PdfReader(resource);
            AcroFields acroFields = pdfReader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
                System.out.println("===== " + name + " =====");
                System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
                System.out.println("Document revision: " + acroFields.getRevision(name) + " of " + acroFields.getTotalRevisions());
                PdfPKCS7 pkcs7 = acroFields.verifySignature(name);
                System.out.println("Subject: " + CertificateInfo.getSubjectFields(pkcs7.getSigningCertificate()));
                System.out.println("Integrity check OK? " + pkcs7.verify());
            }
            System.out.println();
        }
    }

    @Test
    public void testVerifyHideAndReplaceShadowFileSigned2() throws IOException, GeneralSecurityException {
        System.out.println("\n\nhide-and-replace-shadow-file-signed-2.pdf\n======");
        try (   InputStream resource = getClass().getResourceAsStream("hide-and-replace-shadow-file-signed-2.pdf")    ) {
            PdfReader pdfReader = new PdfReader(resource);
            AcroFields acroFields = pdfReader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
                System.out.println("===== " + name + " =====");
                System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
                System.out.println("Document revision: " + acroFields.getRevision(name) + " of " + acroFields.getTotalRevisions());
                PdfPKCS7 pkcs7 = acroFields.verifySignature(name);
                System.out.println("Subject: " + CertificateInfo.getSubjectFields(pkcs7.getSigningCertificate()));
                System.out.println("Integrity check OK? " + pkcs7.verify());
            }
            System.out.println();
        }
    }

    @Test
    public void testVerifyHideAndReplaceShadowFileSigned3() throws IOException, GeneralSecurityException {
        System.out.println("\n\nhide-and-replace-shadow-file-signed-3.pdf\n======");
        try (   InputStream resource = getClass().getResourceAsStream("hide-and-replace-shadow-file-signed-3.pdf")    ) {
            PdfReader pdfReader = new PdfReader(resource);
            AcroFields acroFields = pdfReader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
                System.out.println("===== " + name + " =====");
                System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
                System.out.println("Document revision: " + acroFields.getRevision(name) + " of " + acroFields.getTotalRevisions());
                PdfPKCS7 pkcs7 = acroFields.verifySignature(name);
                System.out.println("Subject: " + CertificateInfo.getSubjectFields(pkcs7.getSigningCertificate()));
                System.out.println("Integrity check OK? " + pkcs7.verify());
            }
            System.out.println();
        }
    }
}
