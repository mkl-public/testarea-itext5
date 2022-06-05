package mkl.testarea.itext5.signature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSigLockDictionary;
import com.itextpdf.text.pdf.PdfSigLockDictionary.LockPermissions;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PrivateKeySignature;

/**
 * Test creating signatures with locks.
 * 
 * @author mkl
 */
public class CreateLockingSignature {
    final static File RESULT_FOLDER = new File("target/test-outputs", "signature");

    public static final String KEYSTORE = "keystores/demo-rsa2048.ks"; 
    public static final char[] PASSWORD = "demo-rsa2048".toCharArray(); 

    public static KeyStore ks = null;
    public static PrivateKey pk = null;
    public static Certificate[] chain = null;

    @BeforeClass
    public static void setUp() throws Exception {
        RESULT_FOLDER.mkdirs();

        BouncyCastleProvider bcp = new BouncyCastleProvider();
        //Security.addProvider(bcp);
        Security.insertProviderAt(bcp, 1);

        ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(KEYSTORE), PASSWORD);
        String alias = (String) ks.aliases().nextElement();
        pk = (PrivateKey) ks.getKey(alias, PASSWORD);
        chain = ks.getCertificateChain(alias);
    }

    /**
     * <a href="https://stackoverflow.com/questions/72467694/lock-pdf-with-itext-after-sign">
     * lock pdf with itext after sign
     * </a>
     * <p>
     * This test illustrates how to create a signature that locks the PDF.
     * </p>
     */
    @Test
    public void signWithLockNoChangesAllowed() throws IOException, DocumentException, GeneralSecurityException {
        try (   InputStream pdfResource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "test-signed-and-locked.pdf"));   ) {
            PdfReader reader = new PdfReader(pdfResource);
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "Signature");

            PdfSigLockDictionary pdfSigLockDictionary = new PdfSigLockDictionary(LockPermissions.NO_CHANGES_ALLOWED);
            appearance.setFieldLockDict(pdfSigLockDictionary);

            ExternalSignature pks = new PrivateKeySignature(pk, "SHA512", "BC");
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, pks, chain,
                null, null, null, 0, CryptoStandard.CMS);
        }
    }

}
