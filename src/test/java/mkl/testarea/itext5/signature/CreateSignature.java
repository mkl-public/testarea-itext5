package mkl.testarea.itext5.signature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PrivateKeySignature;

/**
 * Miscellaneous signing tests.
 * 
 * @author mkl
 */
public class CreateSignature
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "signature");

    public static final String KEYSTORE = "keystores/demo-rsa2048.ks"; 
    public static final char[] PASSWORD = "demo-rsa2048".toCharArray(); 

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

        ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(KEYSTORE), PASSWORD);
        String alias = (String) ks.aliases().nextElement();
        pk = (PrivateKey) ks.getKey(alias, PASSWORD);
        chain = ks.getCertificateChain(alias);
    }

    /**
     * <a href="http://stackoverflow.com/questions/30449348/signing-pdf-memory-consumption">
     * Signing PDF - memory consumption
     * </a>
     * <br>
     * <a href="http://50mpdf.tk/50m.pdf">50m.pdf</a>
     * <p>
     * {@link #sign50MNaive()} tests the naive approach,
     * {@link #sign50MBruno()} tests Bruno's original approach,
     * {@link #sign50MBrunoPartial()} tests Bruno's approach with partial reading,
     * {@link #sign50MBrunoAppend()} tests Bruno's approach with append mode, and
     * {@link #sign50MBrunoPartialAppend()} tests Bruno's approach with partial reading and append mode.
     * </p>
     */
    // runs with -Xmx240m, fails with -Xmx230m
    @Test
    public void sign50MNaive() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/50m.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "50m-signedNaive.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0');
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    //runs with -Xmx81m, fails with -Xmx80m
    @Test
    public void sign50MBruno() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/50m.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "50m-signedBruno.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, false);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    //runs with -Xmx81m, fails with -Xmx80m
    @Test
    public void sign50MBrunoPartial() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/50m.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "50m-signedBrunoPartial.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, false);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    //runs with -Xmx7m, fails with -Xmx6m
    @Test
    public void sign50MBrunoAppend() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/50m.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "50m-signedBrunoAppend.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    //runs with -Xmx7m, fails with -Xmx6m
    @Test
    public void sign50MBrunoPartialAppend() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/50m.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "50m-signedBrunoPartialAppend.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }
}
