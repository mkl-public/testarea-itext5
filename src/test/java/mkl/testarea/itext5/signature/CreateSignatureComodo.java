package mkl.testarea.itext5.signature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorException;
import org.bouncycastle.x509.util.StreamParsingException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSigLockDictionary;
import com.itextpdf.text.pdf.PdfSigLockDictionary.LockPermissions;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.CrlClient;
import com.itextpdf.text.pdf.security.CrlClientOnline;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.OcspClientBouncyCastle;
import com.itextpdf.text.pdf.security.PrivateKeySignature;

/**
 * Signing tests using a Comodo free email certificate.
 * 
 * @author mkl
 */
public class CreateSignatureComodo {
    final static File RESULT_FOLDER = new File("target/test-outputs", "signature");

    public static final String KEYSTORE = "d:/Archive/Security/Comodo Test1234.pfx"; 
    public static final char[] PASSWORD = "Test1234".toCharArray(); 

    public static KeyStore ks = null;
    public static PrivateKey pk = null;
    public static Certificate[] chain = null;

    @BeforeClass
    public static void setUp() throws Exception
    {
        RESULT_FOLDER.mkdirs();

        BouncyCastleProvider bcp = new BouncyCastleProvider();
        //Security.addProvider(bcp);
        Security.insertProviderAt(bcp, 1);

        ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(KEYSTORE), PASSWORD);
        String alias = (String) ks.aliases().nextElement();
        pk = (PrivateKey) ks.getKey(alias, PASSWORD);
        chain = ks.getCertificateChain(alias);
    }

    /**
     * <a href="https://stackoverflow.com/questions/53297489/how-to-enable-ltv-for-a-timestamp-signature-and-set-the-pdf-change-not-allowed">
     * How to enable LTV for a timestamp signature and set the pdf change not allowed?
     * </a>
     * <p>
     * This test creates a simple signature.
     * </p>
     */
    @Test
    public void testCreateSimpleSignature() throws IOException, DocumentException, GeneralSecurityException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                OutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "test-comodo-simple.pdf"))) {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);

            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("reason");
            appearance.setLocation("location");
            appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");

            ExternalSignature pks = new PrivateKeySignature(pk, "SHA512", "BC");
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, CryptoStandard.CMS);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/53297489/how-to-enable-ltv-for-a-timestamp-signature-and-set-the-pdf-change-not-allowed">
     * How to enable LTV for a timestamp signature and set the pdf change not allowed?
     * </a>
     * <p>
     * This test creates a no-changes-allowed certification and thereafter
     * adds LTV information. Adobe does not accept this in spite of using
     * a PDF-2.0 file or a PDF-1.7 file with applicable developer extensions.
     * </p>
     */
    @Test
    public void testCreateNoChangesAllowedCertificationAndLtv() throws IOException, DocumentException, GeneralSecurityException, OperatorException, StreamParsingException, OCSPException {
        try (   InputStream resource = getClass().getResourceAsStream(/*"/mkl/testarea/itext5/extract/test.pdf"*/"test-2.0.pdf");
                OutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "test-comodo-no-changes-allowed.pdf"))) {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);

            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("reason");
            appearance.setLocation("location");
            appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
            appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
//            appearance.addDeveloperExtension(new PdfDeveloperExtension(PdfName.ESIC, PdfWriter.PDF_VERSION_1_7, 1));
//            appearance.addDeveloperExtension(new PdfDeveloperExtension(PdfName.ESIC, PdfWriter.PDF_VERSION_1_7, 2));
//            appearance.addDeveloperExtension(new PdfDeveloperExtension(PdfName.ESIC, PdfWriter.PDF_VERSION_1_7, 5));
//            appearance.addDeveloperExtension(new PdfDeveloperExtension(PdfName.ADBE, PdfWriter.PDF_VERSION_1_7, 8));

            ExternalSignature pks = new PrivateKeySignature(pk, "SHA512", "BC");
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, CryptoStandard.CADES);
        }

        try (   InputStream resource = new FileInputStream(new File(RESULT_FOLDER, "test-comodo-no-changes-allowed.pdf"));
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "test-comodo-no-changes-allowed-ltv.pdf"))) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result, (char)0, true);

            AdobeLtvEnabling adobeLtvEnabling = new AdobeLtvEnabling(pdfStamper);
            OcspClient ocsp = new OcspClientBouncyCastle();
            CrlClient crl = new CrlClientOnline();
            adobeLtvEnabling.enable(ocsp, crl);

            pdfStamper.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/53297489/how-to-enable-ltv-for-a-timestamp-signature-and-set-the-pdf-change-not-allowed">
     * How to enable LTV for a timestamp signature and set the pdf change not allowed?
     * </a>
     * <p>
     * This test creates a form-filling certification, thereafter adds
     * LTV information, and finally signs again making use of a signature
     * lock dictionary that causes a FieldMDP transform that reduces
     * the access to no-changes-allowed. Adobe accepts this but the
     * incremental update can easily be removed and the remains would be
     * form-filling allowed again.
     * </p>
     */
    @Test
    public void testCreateCertificationAndLtvAndNoChangesAllowed() throws IOException, DocumentException, GeneralSecurityException, OperatorException, StreamParsingException, OCSPException {
        try (   InputStream resource = getClass().getResourceAsStream(/*"/mkl/testarea/itext5/extract/test.pdf"*/"test-2.0.pdf");
                OutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "test-comodo-form-filling.pdf"))) {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);

            PdfFormField field = PdfFormField.createSignature(stamper.getWriter());
            field.setFieldName("app");
            field.setWidget(new Rectangle(30, 830, 170, 770), PdfAnnotation.HIGHLIGHT_NONE);
            PdfSigLockDictionary lock = new PdfSigLockDictionary(LockPermissions.NO_CHANGES_ALLOWED);
            field.put(PdfName.LOCK, stamper.getWriter().addToBody(lock).getIndirectReference());
            stamper.addAnnotation(field, 1);

            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("reason");
            appearance.setLocation("location");
            appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
            appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_FORM_FILLING);
//            appearance.addDeveloperExtension(new PdfDeveloperExtension(PdfName.ESIC, PdfWriter.PDF_VERSION_1_7, 1));
//            appearance.addDeveloperExtension(new PdfDeveloperExtension(PdfName.ESIC, PdfWriter.PDF_VERSION_1_7, 2));
//            appearance.addDeveloperExtension(new PdfDeveloperExtension(PdfName.ESIC, PdfWriter.PDF_VERSION_1_7, 5));
//            appearance.addDeveloperExtension(new PdfDeveloperExtension(PdfName.ADBE, PdfWriter.PDF_VERSION_1_7, 8));

            ExternalSignature pks = new PrivateKeySignature(pk, "SHA512", "BC");
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, CryptoStandard.CADES);
        }

        try (   InputStream resource = new FileInputStream(new File(RESULT_FOLDER, "test-comodo-form-filling.pdf"));
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "test-comodo-form-filling-ltv.pdf"))) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result, (char)0, true);

            AdobeLtvEnabling adobeLtvEnabling = new AdobeLtvEnabling(pdfStamper);
            OcspClient ocsp = new OcspClientBouncyCastle();
            CrlClient crl = new CrlClientOnline();
            adobeLtvEnabling.enable(ocsp, crl);

            pdfStamper.close();
        }

        try (   InputStream resource = new FileInputStream(new File(RESULT_FOLDER, "test-comodo-form-filling.pdf"));
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "test-comodo-form-filling-ltv-no-changes-allowed.pdf"))) {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = PdfStamper.createSignature(reader, result, '\0', RESULT_FOLDER, true);

            AdobeLtvEnabling adobeLtvEnabling = new AdobeLtvEnabling(stamper);
            OcspClient ocsp = new OcspClientBouncyCastle();
            CrlClient crl = new CrlClientOnline();
            adobeLtvEnabling.enable(ocsp, crl);

            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("reason");
            appearance.setLocation("location");
            appearance.setVisibleSignature("app");

            ExternalSignature pks = new PrivateKeySignature(pk, "SHA512", "BC");
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, CryptoStandard.CADES);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/53297489/how-to-enable-ltv-for-a-timestamp-signature-and-set-the-pdf-change-not-allowed">
     * How to enable LTV for a timestamp signature and set the pdf change not allowed?
     * </a>
     * <p>
     * This test creates a no-changes-allowed certification and in that
     * process also adds LTV information. Adobe does accept this.
     * </p>
     */
    @Test
    public void testCreateLtvNoChangesAllowedCertification() throws IOException, DocumentException, GeneralSecurityException, OperatorException, StreamParsingException, OCSPException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                OutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "test-comodo-ltv-no-changes-allowed.pdf"))) {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);

            AdobeLtvEnabling adobeLtvEnabling = new AdobeLtvEnabling(stamper);
            OcspClient ocsp = new OcspClientBouncyCastle();
            CrlClient crl = new CrlClientOnline();
            adobeLtvEnabling.addLtvForChain((X509Certificate) chain[0], ocsp, crl, PdfName.A);
            adobeLtvEnabling.outputDss();

            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("reason");
            appearance.setLocation("location");
            appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
            appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);

            ExternalSignature pks = new PrivateKeySignature(pk, "SHA512", "BC");
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, CryptoStandard.CADES);
        }
    }
}
