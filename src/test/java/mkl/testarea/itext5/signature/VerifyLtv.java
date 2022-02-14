package mkl.testarea.itext5.signature;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.util.List;

import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.log.LoggerFactory;
import com.itextpdf.text.log.SysoLogger;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.LtvVerifier;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.VerificationException;
import com.itextpdf.text.pdf.security.VerificationOK;

/**
 * @author mkl
 */
public class VerifyLtv {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * <a href="https://stackoverflow.com/questions/71080881/itext-verify-signature-with-checking-crl">
     * iText Verify Signature with checking CRL
     * </a>
     * <br/>
     * <a href="https://www.grosfichiers.com/i4fmqCz43is">
     * CURRENT_SIGNATURE.pdf
     * </a>
     * <p>
     * A first attempt using the iText LtvVerifier class. Unfortunately
     * that class is designed for a very specific use case, and even
     * after tweaking does not return the desired results.
     * </p>
     */
    @Test
    public void testLtvVerifyCURRENT_SIGNATURE() throws IOException, GeneralSecurityException {
        LoggerFactory.getInstance().setLogger(new SysoLogger());
        try (   InputStream resource = getClass().getResourceAsStream("CURRENT_SIGNATURE.pdf")  ) {
            PdfReader pdfReader = new PdfReader(resource);
            LtvVerifier ltvVerifier = new LtvVerifier(pdfReader) {
                @Override
                protected PdfPKCS7 coversWholeDocument() throws GeneralSecurityException {
                    PdfPKCS7 pkcs7 = fields.verifySignature(signatureName);
                    if (fields.signatureCoversWholeDocument(signatureName)) {
                        LOGGER.info("Covers the whole document.");
                    }
                    else {
                        LOGGER.info("Does not cover the whole document.");
                    }
                    if (pkcs7.verify()) {
                        LOGGER.info("The signed document has not been modified.");
                        return pkcs7;
                    }
                    else {
                        throw new VerificationException(null, "The document was altered after the final signature was applied.");
                    }
                }
                
            };
            //ltvVerifier.setOnlineCheckingAllowed(false);
            List<VerificationOK> results = ltvVerifier.verifySignature();
            System.out.println(results.size());
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/71080881/itext-verify-signature-with-checking-crl">
     * iText Verify Signature with checking CRL
     * </a>
     * <br/>
     * <a href="https://www.grosfichiers.com/i4fmqCz43is">
     * CURRENT_SIGNATURE.pdf
     * </a>
     * <p>
     * Here CRLs and OCSP responses are extracted directly from the PDF
     * DSS. The extraction code is borrowed from the iText LtvVerifier
     * class.
     * </p>
     */
    @Test
    public void testExtractRevocationInformationCURRENT_SIGNATURE() throws IOException, GeneralSecurityException {
        try (   InputStream resource = getClass().getResourceAsStream("CURRENT_SIGNATURE.pdf")  ) {
            PdfReader pdfReader = new PdfReader(resource);

            PdfDictionary dss = pdfReader.getCatalog().getAsDict(PdfName.DSS);
            if (dss == null)
                System.out.println("No DSS in PDF");
            else {
                PdfArray crlarray = dss.getAsArray(PdfName.CRLS);
                if (crlarray == null || crlarray.size() == 0)
                    System.out.println("No CRLs in DSS");
                else {
                    System.out.println("CRLs:");
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    for (int i = 0; i < crlarray.size(); i++) {
                        PRStream stream = (PRStream) crlarray.getAsStream(i);
                        X509CRL crl = (X509CRL)cf.generateCRL(new ByteArrayInputStream(PdfReader.getStreamBytes(stream)));

                        System.out.printf("  '%s' update %s\n", crl.getIssuerX500Principal(), crl.getThisUpdate());
                    }
                }

                PdfArray ocsparray = dss.getAsArray(PdfName.OCSPS);
                if (ocsparray == null || ocsparray.size() == 0)
                    System.out.println("\nNo OCSP responses in DSS");
                else {
                    System.out.println("\nOCSP Responses:");
                    for (int i = 0; i < ocsparray.size(); i++) {
                        PRStream stream = (PRStream) ocsparray.getAsStream(i);
                        OCSPResp ocspResponse = new OCSPResp(PdfReader.getStreamBytes(stream));
                        if (ocspResponse.getStatus() == 0) {
                            try {
                                BasicOCSPResp basicOCSPResp = (BasicOCSPResp) ocspResponse.getResponseObject();
                                System.out.printf("  '%s' update %s\n", basicOCSPResp.getResponderId(), basicOCSPResp.getProducedAt());
                            } catch (OCSPException e) {
                                throw new GeneralSecurityException(e);
                            }
                        }
                    }
                }
            }
        }
    }
}
