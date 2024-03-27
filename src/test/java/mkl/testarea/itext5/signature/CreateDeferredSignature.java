package mkl.testarea.itext5.signature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalBlankSignatureContainer;
import com.itextpdf.text.pdf.security.ExternalSignatureContainer;
import com.itextpdf.text.pdf.security.MakeSignature;

/**
 * @author mkl
 */
public class CreateDeferredSignature {
    final static File RESULT_FOLDER = new File("target/test-outputs", "signature");

    @BeforeClass
    public static void setUp() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/78220445/itext-pdf-deferred-signing-with-invalid-signature">
     * Itext pdf deferred signing with invalid signature
     * </a>
     * <p>
     * This test shows how to use deferred signing with signature creation services
     * that return CMS / PKCS#7 signature containers.
     * </p>
     */
    @Test
    public void testExternalSignatureContainer() throws IOException, DocumentException, GeneralSecurityException {
        File tempFile = new File(RESULT_FOLDER, "deferredContainerSigningTemp.pdf");
        File resultFile = new File(RESULT_FOLDER, "deferredContainerSigning.pdf");
        String fieldName = "Signature";

        byte[] hash = null;

        try (
            InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
            OutputStream tempOutput = new FileOutputStream(tempFile)
        ) {
            PdfReader pdfReader = new PdfReader(resource);
            hash = prepareAndHashForSigning(pdfReader, tempOutput, fieldName);
        }

        // execute your signature container request for the calculated hash here 
        byte[] signatureContainer = retrievePkcs7ContainerForHash(hash);

        try (
            OutputStream resultOutput = new FileOutputStream(resultFile)
        ) {
            PdfReader pdfReader = new PdfReader(tempFile.getPath());
            injectPkcs7Container(pdfReader, resultOutput, fieldName, signatureContainer);
        }
    }

    /**
     * This method adds a signature field to the given PDF and sets its value signature
     * dictionary. The placeholder for the signature container therein is filled with 0s.
     * 
     * @return the hash of the signed byte ranges of the added signature.
     * @see #testExternalSignatureContainer()
     */
    byte[] prepareAndHashForSigning(PdfReader pdfReader, OutputStream outputStream, String fieldName) throws DocumentException, IOException, GeneralSecurityException {
        PdfStamper pdfStamper = PdfStamper.createSignature(pdfReader, outputStream, (char) 0);
        PdfSignatureAppearance pdfSignatureAppearance = pdfStamper.getSignatureAppearance();
        pdfSignatureAppearance.setVisibleSignature(new Rectangle(50, 680, 144, 780), 1, fieldName);

        ExternalSignatureContainer blankContainer = new ExternalBlankSignatureContainer(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
        MakeSignature.signExternalContainer(pdfSignatureAppearance, blankContainer, 12000);
        InputStream rangeStream = pdfSignatureAppearance.getRangeStream();
        BouncyCastleDigest bouncyCastleDigest = new BouncyCastleDigest();
        byte[] hash = DigestAlgorithms.digest(rangeStream, bouncyCastleDigest.getMessageDigest("SHA256"));
        return hash;
    }

    /**
     * This method sets the signature container placeholder in the signature dictionary
     * value of the signature field with the given name.
     * 
     * @see #testExternalSignatureContainer()
     */
    void injectPkcs7Container(PdfReader pdfReader, OutputStream outputStream, String fieldName, byte[] signatureContainer) throws DocumentException, IOException, GeneralSecurityException {
        ExternalSignatureContainer injectingContainer = new InjectingSignatureContainer(signatureContainer);
        MakeSignature.signDeferred(pdfReader, fieldName, outputStream, injectingContainer);
    }

    /**
     * This is a dummy method that returns the given hash itself instead of a signature
     * container for it. Obviously, it is not for production purposes.
     * 
     * @see #testExternalSignatureContainer()
     */
    byte[] retrievePkcs7ContainerForHash(byte[] hash) {
        return hash;
    }

    /**
     * This {@link ExternalSignatureContainer} implementation returns a pre-generated
     * byte array in its {@link #sign(InputStream)} method.
     */
    static class InjectingSignatureContainer implements ExternalSignatureContainer {
        final byte[] signatureContainer;

        public InjectingSignatureContainer(byte[] signatureContainer) {
            this.signatureContainer = signatureContainer;
        }

        @Override
        public byte[] sign(InputStream data) throws GeneralSecurityException {
            return signatureContainer;
        }

        @Override
        public void modifySigningDictionary(PdfDictionary signDic) {
        }
    }
}
