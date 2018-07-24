package mkl.testarea.itext5.signature;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse;
import org.bouncycastle.asn1.ocsp.OCSPResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfName;

/**
 * @author mkl
 */
public class CreateVriKey {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        BouncyCastleProvider bcp = new BouncyCastleProvider();
        Security.insertProviderAt(bcp, 1);
    }

    /**
     * <a href="https://stackoverflow.com/questions/51370965/how-can-i-add-pades-ltv-using-itext">
     * how can I add PAdES-LTV using itext
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/18xiNeLZG0jcz3HGxa5qAug3szpRuJqvw/view?usp=sharing">
     * sign_without_LTV.pdf
     * </a>
     * <p>
     * This method tests LTV VRI signature hash key generation for an OCSP response.
     * </p>
     * <p>
     * The OCSP response has been retrieved from the PDF Adobe Acrobat generated from the
     * given document in the course of LTV enabling. The expected key observed in the LTV
     * enabled document is /6B368A280527D973D359FB2F53DEFD73F7DF3A07.
     * </p>
     */
    @Test
    public void testOcsp() throws IOException, NoSuchAlgorithmException {
        System.out.print("OCSP signature name: ");
        try (   InputStream resource = getClass().getResourceAsStream("sign_with_Adobe_LTV.pdf-ocsp.ocsp")) {
            byte[] resourceBytes = IOUtils.toByteArray(resource);
            OCSPResponse fullResponse = OCSPResponse.getInstance(resourceBytes);
            byte[] basicResponseBytes = fullResponse.getResponseBytes().getResponse().getOctets();
            BasicOCSPResponse basicResponse = BasicOCSPResponse.getInstance(basicResponseBytes);
            byte[] signatureBytes = basicResponse.getSignature().getBytes();
            DEROctetString octetString = new DEROctetString(signatureBytes);
            byte[] octetBytes = octetString.getEncoded();
            byte[] octetHash = hashBytesSha1(octetBytes);
            PdfName octetName = new PdfName(Utilities.convertToHex(octetHash));
            octetName.toPdf(null, System.out);
            System.out.println();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/51370965/how-can-i-add-pades-ltv-using-itext">
     * how can I add PAdES-LTV using itext
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/18xiNeLZG0jcz3HGxa5qAug3szpRuJqvw/view?usp=sharing">
     * sign_without_LTV.pdf
     * </a>
     * <p>
     * This method tests LTV VRI signature hash key generation for a CRL.
     * </p>
     * <p>
     * The CRL has been retrieved from the PDF Adobe Acrobat generated from the given
     * document in the course of LTV enabling. The expected key observed in the LTV
     * enabled document is /FE65791D131511C8EB9931A1B6C3B9BD04ED2B02.
     * </p>
     */
    @Test
    public void testCrl1() throws IOException, NoSuchAlgorithmException, CertificateException, CRLException {
        System.out.print("CRL 1 signature name: ");
        try (   InputStream resource = getClass().getResourceAsStream("sign_with_Adobe_LTV.pdf-crl-1.crl")) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509CRL crl = (X509CRL)cf.generateCRL(resource);
            byte[] signatureBytes = crl.getSignature();
            DEROctetString octetString = new DEROctetString(signatureBytes);
            byte[] octetBytes = octetString.getEncoded();
            byte[] octetHash = hashBytesSha1(octetBytes);
            PdfName octetName = new PdfName(Utilities.convertToHex(octetHash));
            octetName.toPdf(null, System.out);
            System.out.println();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/51370965/how-can-i-add-pades-ltv-using-itext">
     * how can I add PAdES-LTV using itext
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/18xiNeLZG0jcz3HGxa5qAug3szpRuJqvw/view?usp=sharing">
     * sign_without_LTV.pdf
     * </a>
     * <p>
     * This method tests LTV VRI signature hash key generation for a CRL.
     * </p>
     * <p>
     * The CRL has been retrieved from the PDF Adobe Acrobat generated from the given
     * document in the course of LTV enabling. The expected key observed in the LTV
     * enabled document is /BC5CDC76E00EB6509035C7207FC20901DA883C1F.
     * </p>
     */
    @Test
    public void testCrl2() throws IOException, NoSuchAlgorithmException, CertificateException, CRLException {
        System.out.print("CRL 2 signature name: ");
        try (   InputStream resource = getClass().getResourceAsStream("sign_with_Adobe_LTV.pdf-crl-2.crl")) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509CRL crl = (X509CRL)cf.generateCRL(resource);
            byte[] signatureBytes = crl.getSignature();
            DEROctetString octetString = new DEROctetString(signatureBytes);
            byte[] octetBytes = octetString.getEncoded();
            byte[] octetHash = hashBytesSha1(octetBytes);
            PdfName octetName = new PdfName(Utilities.convertToHex(octetHash));
            octetName.toPdf(null, System.out);
            System.out.println();
        }
    }

    private static byte[] hashBytesSha1(byte[] b) throws NoSuchAlgorithmException {
        MessageDigest sh = MessageDigest.getInstance("SHA1");
        return sh.digest(b);
    }
}
