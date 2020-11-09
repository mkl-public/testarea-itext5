package mkl.testarea.itext5.signature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.io.StreamUtil;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.CertificateInfo.X500Name;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.ExternalSignatureContainer;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PdfPKCS7;
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

    /**
     * <a href="http://stackoverflow.com/questions/30526254/sign-concatenated-pdf-in-append-mode-with-certified-no-changes-allowed">
     * Sign concatenated PDF in append mode with CERTIFIED_NO_CHANGES_ALLOWED
     * </a>
     * <br>
     * <a href="https://www.dropbox.com/s/lea6r9fup6th44c/test_pdf.zip?dl=0">test_pdf.zip</a>
     * 
     * {@link #signCertifyG()} certifies g.pdf, OK
     * {@link #sign2g()} merely signs 2g.pdf, OK
     * {@link #signCertify2gNoAppend()} certifies 2g.pdf but not in append mode, OK
     * {@link #tidySignCertify2g()} first tidies, then certifies 2g.pdf, OK
     * {@link #signCertify2g()} certifies 2g.pdf, Adobe says invalid
     * {@link #signCertify2gFix()} certifies 2g-fix.pdf, OK!
     * 
     * 2g-fix.pdf is a patched version of 2g.pdf with a valid /Size trailer entry
     * and a valid, single-sectioned cross reference table 
     */
    @Test
    public void signCertifyG() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/g.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "g-certified.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    @Test
    public void sign2g() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/2g.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "2g-signed.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        //appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    @Test
    public void signCertify2g() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/2g.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "2g-certified.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    @Test
    public void signCertify2gNoAppend() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/2g.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "2g-certified-noAppend.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    @Test
    public void signCertify2gFix() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/2g-fix.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "2g-fix-certified.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }
    
    @Test
    public void tidySignCertify2g() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/2g.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Tidying
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "2g-tidied.pdf"));
        PdfStamper stamper = new  PdfStamper(reader, os);
        stamper.close();

        // Creating the reader and the stamper
        reader = new PdfReader(new File(RESULT_FOLDER, "2g-tidied.pdf").toString(), null, true);
        os = new FileOutputStream(new File(RESULT_FOLDER, "2g-tidied-certified.pdf"));
        stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }
    
    /**
     * <a href="http://stackoverflow.com/questions/36589698/error-while-digitally-signing-a-pdf">
     * Error while digitally signing a PDF
     * </a>
     * <p>
     * Tried to reproduce the OP's issue with my own test PDF and key. But it worked alright.
     * </p>
     */
    @Test
    public void signLikeJackSparrow() throws GeneralSecurityException, IOException, DocumentException
    {
        final String SRC      = "src/test/resources/mkl/testarea/itext5/extract/test.pdf";
        final String DEST     = new File(RESULT_FOLDER, "test-JackSparrow-%s.pdf").getPath();

        C2_01_SignHelloWorld_sign(SRC, String.format(DEST, 1), chain, pk, DigestAlgorithms.SHA256, "BC", CryptoStandard.CMS, "Signed for Testing", "Universe");
        C2_01_SignHelloWorld_sign(SRC, String.format(DEST, 2), chain, pk, DigestAlgorithms.SHA512, "BC", CryptoStandard.CMS, "Test 2", "Universe");
        C2_01_SignHelloWorld_sign(SRC, String.format(DEST, 3), chain, pk, DigestAlgorithms.SHA256, "BC", CryptoStandard.CADES, "Test 3", "Universe");
    }
    
    public void C2_01_SignHelloWorld_sign(String src, String dest, Certificate[] chain, PrivateKey pk, String digestAlgorithm, String provider, CryptoStandard subfilter, String reason, String location)
            throws GeneralSecurityException, IOException, DocumentException {
        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(src);
        FileOutputStream os = new FileOutputStream(dest);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason(reason);
        appearance.setLocation(location);
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalDigest digest = new BouncyCastleDigest();
        ExternalSignature signature = new PrivateKeySignature(pk, digestAlgorithm, provider);
        MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, subfilter);
    }

    /**
     * <a href="https://stackoverflow.com/questions/45062602/itext-pdfappearence-issue">
     * Text - PDFAppearence issue
     * </a>
     * <p>
     * This test shows how one can create a custom signature layer 2.
     * As the OP of the question at hand mainly wants to generate a
     * pure DESCRIPTION appearance that uses the whole area, we here
     * essentially copy the PdfSignatureAppearance.getAppearance code
     * for generating layer 2 in pure DESCRIPTION mode and apply it
     * to a plain pre-fetched layer 2.
     * </p>
     */
    @Test
    public void signWithCustomLayer2() throws IOException, DocumentException, GeneralSecurityException
    {
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf")  )
        {
            PdfReader reader = new PdfReader(resource);
            FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "test-customLayer2.pdf"));
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');

            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("reason");
            appearance.setLocation("location");
            appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");

            // This essentially is the PdfSignatureAppearance.getAppearance code
            // for generating layer 2 in pure DESCRIPTION mode applied to a plain
            // pre-fetched layer 2.
            // vvvvv
            PdfTemplate layer2 = appearance.getLayer(2);
            String text = "We're using iText to put a text inside a signature placeholder in a PDF. "
                    + "We use a code snippet similar to this to define the Signature Appearence.\n"
                    + "Everything works fine, but the signature text does not fill all the signature "
                    + "placeholder area as expected by us, but the area filled seems to have an height "
                    + "that is approximately the 70% of the available space.\n"
                    + "As a result, sometimes especially if the length of the signature text is quite "
                    + "big, the signature text does not fit in the placeholder and the text is striped "
                    + "away.";
            Font font = new Font();
            float size = font.getSize();
            final float MARGIN = 2;
            Rectangle dataRect = new Rectangle(
                    MARGIN,
                    MARGIN,
                    appearance.getRect().getWidth() - MARGIN,
                    appearance.getRect().getHeight() - MARGIN);
            if (size <= 0) {
                Rectangle sr = new Rectangle(dataRect.getWidth(), dataRect.getHeight());
                size = ColumnText.fitText(font, text, sr, 12, appearance.getRunDirection());
            }
            ColumnText ct = new ColumnText(layer2);
            ct.setRunDirection(appearance.getRunDirection());
            ct.setSimpleColumn(new Phrase(text, font), dataRect.getLeft(), dataRect.getBottom(), dataRect.getRight(), dataRect.getTop(), size, Element.ALIGN_LEFT);
            ct.go();
            // ^^^^^

            ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, subfilter);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/49861696/delete-padding-of-rectangle-in-itext-pdf-signature">
     * Delete padding of Rectangle in iText PDF signature
     * </a>
     * <p>
     * The overlapping-rectangles issue can be resolved by choosing
     * rectangle coordinates in a non-overlapping manner, cf.
     * {@link #tuneAppearanceLikeJoseJavierHernándezBenítez(PdfSignatureAppearance, int, String)}.
     * The other issue, the free space at the top, can be resolved
     * as shown in the test {@link #signWithCustomLayer2()} above.
     * </p>
     */
    @Test
    public void signInSmallRectangles() throws IOException, DocumentException, GeneralSecurityException {
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                OutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "smallRectangles1.pdf"))) {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);

            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("reason");
            appearance.setLocation("location");
            tuneAppearanceLikeJoseJavierHernándezBenítez(appearance, 1, "Sig1");

            ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, subfilter);
        }


        try (   InputStream is = new FileInputStream(new File(RESULT_FOLDER, "smallRectangles1.pdf"));
                OutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "smallRectangles2.pdf"))) {
            PdfReader reader = new PdfReader(is);
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);

            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("reason");
            appearance.setLocation("location");
            tuneAppearanceLikeJoseJavierHernándezBenítez(appearance, 2, "Sig2");

            ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, subfilter);
        }
    }

    // @see #signInSmallRectangles()
    void tuneAppearanceLikeJoseJavierHernándezBenítez(PdfSignatureAppearance signatureAppearance, int next, String contact) {
        signatureAppearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.DESCRIPTION);
        Rectangle rectangle = new Rectangle(
                        36,
                        760 - 20 * (next - 1) , // this is one possible correction of the original: 748 - 20 * (next - 1) ,
                        144,
                        780 - 20 * (next - 1)
            );
        rectangle.normalize();
        signatureAppearance.setVisibleSignature(
                rectangle, 
                1, contact);
    }

    /**
     * <a href="https://stackoverflow.com/questions/52220196/itext5-x-setting-pushbutton-appearance-without-breaking-seal">
     * iText5.x Setting pushbutton appearance without breaking Seal
     * </a>
     * <br/>
     * <a href="http://www.filedropper.com/samplecert">
     * sampleCert.pdf
     * </a>, cleared from signature, reduced to approval as sampleSigClean.pdf
     * <p>
     * This test places a signature into the (formerly certification, now only
     * approval) signature field of the document.
     * </p>
     */
    @Test
    public final void signDeferredSampleSigClean() throws IOException, DocumentException, GeneralSecurityException {
        try (   InputStream input = getClass().getResourceAsStream("sampleSigClean.pdf");
                OutputStream output = new FileOutputStream(new File(RESULT_FOLDER, "sampleSigClean-signed.pdf"))) {
            PdfReader pdfReader = new PdfReader(input);
            String fieldName = "Signature1";
            ExternalSignatureContainer container = new ExternalSignatureContainer() {
                @Override
                public byte[] sign(InputStream data) throws GeneralSecurityException {
                    ExternalDigest externalDigest = new BouncyCastleDigest();
                    ExternalSignature externalSignature = new PrivateKeySignature(pk, "SHA256", "BC");
                    CryptoStandard sigtype = CryptoStandard.CADES;

                    String hashAlgorithm = externalSignature.getHashAlgorithm();
                    PdfPKCS7 sgn = new PdfPKCS7(null, chain, hashAlgorithm, null, externalDigest, false);
                    byte hash[];
                    try {
                        hash = DigestAlgorithms.digest(data, externalDigest.getMessageDigest(hashAlgorithm));
                    } catch (IOException e) {
                        throw new GeneralSecurityException(e);
                    }
                    byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, null, null, sigtype);
                    byte[] extSignature = externalSignature.sign(sh);
                    sgn.setExternalDigest(extSignature, null, externalSignature.getEncryptionAlgorithm());

                    byte[] encodedSig = sgn.getEncodedPKCS7(hash, null, null, null, sigtype);
                    return encodedSig;
                }

                @Override
                public void modifySigningDictionary(PdfDictionary signDic) { }
            };

            MakeSignature.signDeferred(pdfReader, fieldName, output, container);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/53193048/java-pdf-digital-signature-using-itext-visible-but-not-printable">
     * Java PDF digital signature using iText visible, but not printable
     * </a>
     * <p>
     * This test signs a PDF in two passes, first it adds a signature
     * field <i>not setting the PRINT flag</i>, then it signs the PDF
     * using that signatrue field. The signature visualization of the
     * resulting field is invisible in print. 
     * </p>
     */
    @Test
    public void signWidgetNoPrint() throws IOException, DocumentException, GeneralSecurityException {
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;
        File intermediate = new File(RESULT_FOLDER, "NoPrintSignature-empty.pdf");

        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                OutputStream os = new FileOutputStream(intermediate)) {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, os);
            PdfFormField field = PdfFormField.createSignature(stamper.getWriter());
            field.setFieldName("Signature");
            field.setWidget(new Rectangle(30, 830, 170, 770), PdfAnnotation.HIGHLIGHT_NONE);
            stamper.addAnnotation(field, 1);
            stamper.close();
        }

        try (   InputStream resource = new FileInputStream(intermediate);
                OutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "NoPrintSignature-signed.pdf"))) {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');

            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("reason");
            appearance.setLocation("location");
            appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
            appearance.setVisibleSignature("Signature");

            ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, subfilter);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/58473538/change-digitally-signed-by-key-owner-name">
     * Change Digitally signed by “Key Owner Name”
     * </a>
     * <p>
     * This test shows how one can create a custom signature layer 2
     * text including variable certificate information. We here
     * essentially copy the PdfSignatureAppearance.getAppearance code
     * for generating layer 2 text and modify it to work as desired.
     * </p>
     */
    @Test
    public void signWithCustomLayer2Text() throws IOException, DocumentException, GeneralSecurityException
    {
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf")  )
        {
            PdfReader reader = new PdfReader(resource);
            FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "test-customLayer2Text.pdf"));
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');

            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("reason");
            appearance.setLocation("location");
            appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");

            // This essentially is the PdfSignatureAppearance.getAppearance code
            // for generating layer 2 text.
            // vvvvv
            StringBuilder buf = new StringBuilder();
            buf.append("Signed by ");
            String name = null;
            X500Name x500name = CertificateInfo.getSubjectFields((X509Certificate)chain[0]);
            if (x500name != null) {
                name = x500name.getField("CN");
                if (name == null)
                    name = x500name.getField("E");
            }
            if (name == null)
                name = "";
            buf.append(name).append('\n');
            SimpleDateFormat sd = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
            buf.append("Date: ").append(sd.format(appearance.getSignDate().getTime()));
            if (appearance.getReason() != null)
                buf.append('\n').append("Reason: ").append(appearance.getReason());
            if (appearance.getLocation() != null)
                buf.append('\n').append("Location: ").append(appearance.getLocation());
            appearance.setLayer2Text(buf.toString());
            // ^^^^^

            ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, subfilter);
        }
    }

    /**
     * @see #signLikePauloGonçalvesOriginal()
     * @see #signLikePauloGonçalvesEdited()
     */
    public static void signLikePauloGonçalves(InputStream src,OutputStream dest, InputStream p12Stream, char[] password, String reason, String location, String imagePath) throws Exception {
        PdfReader reader = new PdfReader(src);
        PdfStamper stamper = PdfStamper.createSignature(reader, dest, '\0', null, true);
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason(reason);
        appearance.setLocation(location);
        appearance.setVisibleSignature(new Rectangle(300, 600, 630, 500), 1, "sig");

        Image image = Image.getInstance(imagePath);
        appearance.setSignatureGraphic(image);
        appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);

        appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);

        ExternalDigest digest = new BouncyCastleDigest();
        ExternalSignature signature = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, null);
        MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);
    }

    /**
     * <a href="https://stackoverflow.com/questions/62271473/multiple-signings-in-pdf-file-using-itext">
     * Multiple Signings in pdf File using IText
     * </a>
     * <p>
     * This is like the OP's original code. Already the first <code>sign</code>
     * call truncates the original file before iText could read it. Thus, iText
     * throws an exception.
     * </p>
     * @see #signLikePauloGonçalves(InputStream, OutputStream, InputStream, char[], String, String, String)
     */
    @Test
    public void signLikePauloGonçalvesOriginal() throws Exception {
        String basePath = RESULT_FOLDER.getPath() + '/';
        try (   InputStream pdfResource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                InputStream img1Resource = getClass().getResourceAsStream("/mkl/testarea/itext5/content/2x2colored.png");
                InputStream img2Resource = getClass().getResourceAsStream("/mkl/testarea/itext5/stamp/Signature.png")) {
            Files.copy(pdfResource, Paths.get(basePath, "nonsigned.pdf"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(img1Resource, Paths.get(basePath, "signing1.png"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(img2Resource, Paths.get(basePath, "signing2.png"), StandardCopyOption.REPLACE_EXISTING);
        }
        
        signLikePauloGonçalves(new FileInputStream(basePath+"nonsigned.pdf"), new FileOutputStream(basePath+"nonsigned.pdf"), null, "mycert3".toCharArray(), "something", "something", basePath + "signing1.png");
        signLikePauloGonçalves(new FileInputStream(basePath+"nonsigned.pdf"), new FileOutputStream(basePath+"signed.pdf"), null, "mycert4".toCharArray(), "something", "something", basePath + "signing2.png");
    }

    /**
     * <a href="https://stackoverflow.com/questions/62271473/multiple-signings-in-pdf-file-using-itext">
     * Multiple Signings in pdf File using IText
     * </a>
     * <p>
     * This is like the OP's edited code. Both <code>sign</code> calls
     * sign the original, unsigned document. Thus, the remaining output
     * is the file with the signature from the second signing call.
     * </p>
     * @see #signLikePauloGonçalves(InputStream, OutputStream, InputStream, char[], String, String, String)
     */
    @Test
    public void signLikePauloGonçalvesEdited() throws Exception {
        String basePath = RESULT_FOLDER.getPath() + '/';
        try (   InputStream pdfResource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                InputStream img1Resource = getClass().getResourceAsStream("/mkl/testarea/itext5/content/2x2colored.png");
                InputStream img2Resource = getClass().getResourceAsStream("/mkl/testarea/itext5/stamp/Signature.png")) {
            Files.copy(pdfResource, Paths.get(basePath, "nonsigned.pdf"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(img1Resource, Paths.get(basePath, "signing1.png"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(img2Resource, Paths.get(basePath, "signing2.png"), StandardCopyOption.REPLACE_EXISTING);
        }
        
        signLikePauloGonçalves(new FileInputStream(basePath+"nonsigned.pdf"), new FileOutputStream(basePath+"signed.pdf"), null, "mycert3".toCharArray(), "something", "something", basePath + "signing1.png");
        signLikePauloGonçalves(new FileInputStream(basePath+"nonsigned.pdf"), new FileOutputStream(basePath+"signed.pdf"), null, "mycert4".toCharArray(), "something", "something", basePath + "signing2.png");
    }

    /**
     * @see #signLikePauloGonçalvesCorrected()
     */
    public static void signLikePauloGonçalvesImproved(InputStream src, OutputStream dest, InputStream p12Stream, char[] password, String reason, String location, String imagePath, String field) throws Exception {
        PdfReader reader = new PdfReader(src);
        PdfStamper stamper = PdfStamper.createSignature(reader, dest, '\0', null, true);
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason(reason);
        appearance.setLocation(location);
        appearance.setVisibleSignature(new Rectangle(300, 600, 630, 500), 1, field);

        Image image = Image.getInstance(imagePath);
        appearance.setSignatureGraphic(image);
        appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);

//        appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);

        ExternalDigest digest = new BouncyCastleDigest();
        ExternalSignature signature = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, null);
        MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);
    }

    /**
     * <a href="https://stackoverflow.com/questions/62271473/multiple-signings-in-pdf-file-using-itext">
     * Multiple Signings in pdf File using IText
     * </a>
     * <p>
     * This code uses appropriate file streams for the both signing calls.
     * In particular the output of the first signing is the input of the
     * second one, and in no case a signing call uses the same file for
     * input and output.
     * </p>
     * <p>
     * Furthermore, the improved <code>sign</code> method used here allows
     * the use of distinct signature field names for the calls and refrains
     * from setting inappropriate certification levels.
     * </p>
     * @see #signLikePauloGonçalvesImproved(InputStream, OutputStream, InputStream, char[], String, String, String, String)
     */
    @Test
    public void signLikePauloGonçalvesCorrected() throws Exception {
        String basePath = RESULT_FOLDER.getPath() + '/';
        try (   InputStream pdfResource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                InputStream img1Resource = getClass().getResourceAsStream("/mkl/testarea/itext5/content/2x2colored.png");
                InputStream img2Resource = getClass().getResourceAsStream("/mkl/testarea/itext5/stamp/Signature.png")) {
            Files.copy(pdfResource, Paths.get(basePath, "nonsigned.pdf"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(img1Resource, Paths.get(basePath, "signing1.png"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(img2Resource, Paths.get(basePath, "signing2.png"), StandardCopyOption.REPLACE_EXISTING);
        }
        
        signLikePauloGonçalvesImproved(new FileInputStream(basePath+"nonsigned.pdf"), new FileOutputStream(basePath+"signedOnce.pdf"), null, "mycert3".toCharArray(), "something", "something", basePath + "signing1.png", "sig");
        signLikePauloGonçalvesImproved(new FileInputStream(basePath+"signedOnce.pdf"), new FileOutputStream(basePath+"signedTwice.pdf"), null, "mycert4".toCharArray(), "something", "something", basePath + "signing2.png", "sig2");
    }

    /**
     * <a href="https://stackoverflow.com/questions/64661102/attach-digital-signature-to-pdf-using-mssp">
     * Attach digital signature to pdf using mssp
     * </a>
     * <p>
     * This test with its associated {@link RemoteSignatureContainer}
     * class show how in principle sign a PDF using a remote signature
     * server returning a full CMS signature container, not a naked
     * signature as long as that server responds quickly.
     * </p>
     */
    @Test
    public void signUsingExternalContainer() throws IOException, DocumentException, GeneralSecurityException {
        try (   InputStream pdfResource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "test-signed-external-container.pdf"));   ) {
            PdfReader reader = new PdfReader(pdfResource);
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "Signature");
            ExternalSignatureContainer external = new RemoteSignatureContainer();
            MakeSignature.signExternalContainer(appearance, external, 8192);
        }
    }

    /** @see #signUsingExternalContainer() */
    class RemoteSignatureContainer implements ExternalSignatureContainer {
        /**
         * Insert an implementation signing the data from the given
         * Inputstream parameter using the remote server applicable.
         * The code here simply generates a CMS using simple BC
         * classes. 
         */
        @Override
        public byte[] sign(InputStream data) throws GeneralSecurityException {
            try {
                CMSTypedData msg = new CMSProcessableByteArray(StreamUtil.inputStreamToArray(data));

                Store<?> certs = new JcaCertStore(Arrays.asList(chain));

                CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
                ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(pk);

                gen.addSignerInfoGenerator(
                          new JcaSignerInfoGeneratorBuilder(
                               new JcaDigestCalculatorProviderBuilder().setProvider("BC").build())
                               .build(signer, new X509CertificateHolder(chain[0].getEncoded())));

                gen.addCertificates(certs);

                CMSSignedData sigData = gen.generate(msg, false);

                return sigData.getEncoded();
            } catch (IOException | OperatorException | CMSException e) {
                throw new GeneralSecurityException(e);
            }
        }

        @Override
        public void modifySigningDictionary(PdfDictionary signDic) {
            signDic.put(PdfName.FILTER, PdfName.ADOBE_PPKLITE);
            signDic.put(PdfName.SUBFILTER, PdfName.ADBE_PKCS7_DETACHED);
        }
    }
}
