package mkl.testarea.itext5.signature;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.X509CertParser;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OperatorException;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.x509.util.StreamParsingException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.error_messages.MessageLocalization;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDeveloperExtension;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.CrlClient;
import com.itextpdf.text.pdf.security.CrlClientOnline;
import com.itextpdf.text.pdf.security.LtvVerification;
import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.OcspClientBouncyCastle;
import com.itextpdf.text.pdf.security.PdfPKCS7;

/**
 * @author mkl
 */
public class MakeLtvEnabled {
    final static File RESULT_FOLDER = new File("target/test-outputs", "signature");

    @BeforeClass
    public static void setUp() throws Exception {
        RESULT_FOLDER.mkdirs();

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
     * This tests the {@link #makeLtvEnabledV1(PdfStamper, OcspClient, CrlClient)}
     * utility method. The current Adobe Reader accepts the output as LTV enabled
     * after trusting the root certificate of the signer chain.
     * </p>
     */
    @Test
    public void testV1() throws IOException, DocumentException, GeneralSecurityException, StreamParsingException, OperatorCreationException, OCSPException {
        try (   InputStream resource = getClass().getResourceAsStream("sign_without_LTV.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "sign_with_LTV_V1.pdf"))) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result, (char)0, true);

            OcspClient ocsp = new OcspClientBouncyCastle();
            CrlClient crl = new CrlClientOnline();
            makeLtvEnabledV1(pdfStamper, ocsp, crl);

            pdfStamper.close();
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
     * This tests the {@link #makeLtvEnabledV2(PdfStamper, OcspClient, CrlClient)}
     * utility method. The current Adobe Reader accepts the output as LTV enabled
     * after trusting the root certificate of the signer chain.
     * </p>
     */
    @Test
    public void testV2() throws IOException, DocumentException, GeneralSecurityException, StreamParsingException, OperatorCreationException, OCSPException {
        try (   InputStream resource = getClass().getResourceAsStream("sign_without_LTV.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "sign_with_LTV_V2.pdf"))) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result, (char)0, true);

            OcspClient ocsp = new OcspClientBouncyCastle();
            CrlClient crl = new CrlClientOnline();
            makeLtvEnabledV2(pdfStamper, ocsp, crl);

            pdfStamper.close();
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
     * This tests the {@link AdobeLtvEnabling} utility class. The current Adobe Reader accepts
     * the output as LTV enabled after trusting the root certificate of the signer chain.
     * </p>
     */
    @Test
    public void testV3() throws IOException, DocumentException, GeneralSecurityException, StreamParsingException, OCSPException, OperatorException {
        try (   InputStream resource = getClass().getResourceAsStream("sign_without_LTV.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "sign_with_LTV_V3.pdf"))) {
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
     * <a href="https://stackoverflow.com/questions/51370965/how-can-i-add-pades-ltv-using-itext">
     * how can I add PAdES-LTV using itext
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/18xiNeLZG0jcz3HGxa5qAug3szpRuJqvw/view?usp=sharing">
     * sign_without_LTV.pdf
     * </a>
     * <p>
     * This method adds LTV information to the given {@link PdfStamper} to make
     * all signatures LTV enabled with the following restrictions:
     * </p>
     * <ul>
     * <li>signature time stamps are ignored,
     * <li>retrieved CRLs are assumed to be direct and complete,
     * <li>the complete certificate chains are assumed to be buildable using AIA entries.
     * </ul>
     * <p>
     * The code required the following changes to the {@link LtvVerification} class:
     * </p>
     * <ul>
     * <li>Additional {@link LtvVerification#addVerification(PdfName, Collection, Collection, Collection)}
     * overload which accepts a signature hash key instead of a signature field name as first
     * parameter.
     * <li>The private {@link LtvVerification} method <code>outputDss</code> creates <b>TU</b>
     * entries in the <b>VRI</b> dictionaries: <code>vri.put(PdfName.TU, new PdfDate())</code>
     * </ul>
     * <p>
     * This method grew while experimenting. A cleaned up version of it is
     * {@link #makeLtvEnabledV2(PdfStamper, OcspClient, CrlClient)}.
     * </p>
     */
    public void makeLtvEnabledV1(PdfStamper stp, OcspClient ocspClient, CrlClient crlClient) throws IOException, GeneralSecurityException, StreamParsingException, OperatorCreationException, OCSPException {
        LtvVerification v = stp.getLtvVerification();
        AcroFields fields = stp.getAcroFields();

        ArrayList<String> names = fields.getSignatureNames();

        for (String name : names)
        {
            List<byte[]> ocspResponses = new ArrayList<>();
            List<byte[]> crls = new ArrayList<>();
            List<byte[]> certs = new ArrayList<>();
            
            PdfPKCS7 pdfPKCS7 = fields.verifySignature(name);
            X509Certificate issuer = null;
            List<X509Certificate> certificatesToCheck = new ArrayList<>();
            certificatesToCheck.add(pdfPKCS7.getSigningCertificate());
            while (!certificatesToCheck.isEmpty()) {
                X509Certificate certificate = certificatesToCheck.remove(0);
                while (certificate != null) {
                    System.out.println(certificate.getSubjectX500Principal().getName());
                    issuer = getIssuerCertificate(certificate);
                    certs.add(certificate.getEncoded());
                    byte[] ocspResponse = ocspClient.getEncoded(certificate, issuer, null);
                    if (ocspResponse != null) {
                        System.out.println("  with OCSP response");
                        ocspResponses.add(ocspResponse);
                        X509Certificate ocspSigner = getOcspSignerCertificate(ocspResponse);
                        if (ocspSigner != null) {
                            certificatesToCheck.add(ocspSigner);
                            System.out.printf("  signed by %s\n", ocspSigner.getSubjectX500Principal().getName());
                        }
                        v.addVerification(getOcspSignatureKey(ocspResponse), Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
                    } else {
                       Collection<byte[]> crl = crlClient.getEncoded(certificate, null);
                       if (crl != null && !crl.isEmpty()) {
                           System.out.printf("  with %s CRLs\n", crl.size());
                           crls.addAll(crl);
                           for (byte[] crlBytes : crl) {
                               v.addVerification(getCrlSignatureKey(crlBytes), Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
                           }
                       }
                    }
                    certificate = issuer;
                }
            }
            v.addVerification(name, ocspResponses, crls, certs);
        }

        stp.getWriter().addDeveloperExtension(new PdfDeveloperExtension(PdfName.ADBE, new PdfName("1.7"), 8));
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
     * This method adds LTV information to the given {@link PdfStamper} to make
     * all signatures LTV enabled with the following restrictions:
     * </p>
     * <ul>
     * <li>signature time stamps are ignored,
     * <li>retrieved CRLs are assumed to be direct and complete,
     * <li>the complete certificate chains are assumed to be buildable using AIA entries.
     * </ul>
     * <p>
     * The code required the following changes to the {@link LtvVerification} class:
     * </p>
     * <ul>
     * <li>Additional {@link LtvVerification#addVerification(PdfName, Collection, Collection, Collection)}
     * overload which accepts a signature hash key instead of a signature field name as first
     * parameter.
     * <li>The private {@link LtvVerification} method <code>outputDss</code> creates <b>TU</b>
     * entries in the <b>VRI</b> dictionaries: <code>vri.put(PdfName.TU, new PdfDate())</code>
     * </ul>
     * <p>
     * This method is cleaned up. A version of it which grew while experimenting is
     * {@link #makeLtvEnabledV1(PdfStamper, OcspClient, CrlClient)}.
     * </p>
     */
    public void makeLtvEnabledV2(PdfStamper stp, OcspClient ocspClient, CrlClient crlClient) throws IOException, GeneralSecurityException, StreamParsingException, OperatorCreationException, OCSPException {
        stp.getWriter().addDeveloperExtension(new PdfDeveloperExtension(PdfName.ADBE, new PdfName("1.7"), 8));
        LtvVerification v = stp.getLtvVerification();
        AcroFields fields = stp.getAcroFields();

        Map<PdfName, X509Certificate> moreToCheck = new HashMap<>();

        ArrayList<String> names = fields.getSignatureNames();
        for (String name : names)
        {
            PdfPKCS7 pdfPKCS7 = fields.verifySignature(name);
            List<X509Certificate> certificatesToCheck = new ArrayList<>();
            certificatesToCheck.add(pdfPKCS7.getSigningCertificate());
            while (!certificatesToCheck.isEmpty()) {
                X509Certificate certificate = certificatesToCheck.remove(0);
                addLtvForChain(certificate, ocspClient, crlClient,
                        (ocsps, crls, certs) -> {
                            try {
                                v.addVerification(name, ocsps, crls, certs);
                            } catch (IOException | GeneralSecurityException e) {
                                e.printStackTrace();
                            }
                        },
                        moreToCheck::put
                );
            }
        }

        while (!moreToCheck.isEmpty()) {
            PdfName key = moreToCheck.keySet().iterator().next();
            X509Certificate certificate = moreToCheck.remove(key);
            addLtvForChain(certificate, ocspClient, crlClient,
                    (ocsps, crls, certs) -> {
                        try {
                            v.addVerification(key, ocsps, crls, certs);
                        } catch (IOException | GeneralSecurityException e) {
                            e.printStackTrace();
                        }
                    },
                    moreToCheck::put
            );
        }
    }

    /**
     * @see #makeLtvEnabledV2(PdfStamper, OcspClient, CrlClient)
     */
    void addLtvForChain(X509Certificate certificate, OcspClient ocspClient, CrlClient crlClient, VriAdder vriAdder,
            BiConsumer<PdfName, X509Certificate> moreSignersAndCertificates) throws GeneralSecurityException, IOException, StreamParsingException, OperatorCreationException, OCSPException {
        List<byte[]> ocspResponses = new ArrayList<>();
        List<byte[]> crls = new ArrayList<>();
        List<byte[]> certs = new ArrayList<>();
        
        while (certificate != null) {
            System.out.println(certificate.getSubjectX500Principal().getName());
            X509Certificate issuer = getIssuerCertificate(certificate);
            certs.add(certificate.getEncoded());
            byte[] ocspResponse = ocspClient.getEncoded(certificate, issuer, null);
            if (ocspResponse != null) {
                System.out.println("  with OCSP response");
                ocspResponses.add(ocspResponse);
                X509Certificate ocspSigner = getOcspSignerCertificate(ocspResponse);
                if (ocspSigner != null) {
                    System.out.printf("  signed by %s\n", ocspSigner.getSubjectX500Principal().getName());
                }
                moreSignersAndCertificates.accept(getOcspSignatureKey(ocspResponse), ocspSigner);
            } else {
               Collection<byte[]> crl = crlClient.getEncoded(certificate, null);
               if (crl != null && !crl.isEmpty()) {
                   System.out.printf("  with %s CRLs\n", crl.size());
                   crls.addAll(crl);
                   for (byte[] crlBytes : crl) {
                       moreSignersAndCertificates.accept(getCrlSignatureKey(crlBytes), null);
                   }
               }
            }
            certificate = issuer;
        }
        
        vriAdder.accept(ocspResponses, crls, certs);
    }

    /**
     * @see #makeLtvEnabledV1(PdfStamper, OcspClient, CrlClient)
     * @see #makeLtvEnabledV2(PdfStamper, OcspClient, CrlClient)
     */
    static X509Certificate getOcspSignerCertificate(byte[] basicResponseBytes) throws CertificateException, OCSPException, OperatorCreationException {
        JcaX509CertificateConverter converter = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);
        BasicOCSPResponse borRaw = BasicOCSPResponse.getInstance(basicResponseBytes);
        BasicOCSPResp bor = new BasicOCSPResp(borRaw);

        for (final X509CertificateHolder x509CertificateHolder : bor.getCerts()) {
            X509Certificate x509Certificate = converter.getCertificate(x509CertificateHolder);

            JcaContentVerifierProviderBuilder jcaContentVerifierProviderBuilder = new JcaContentVerifierProviderBuilder();
            jcaContentVerifierProviderBuilder.setProvider(BouncyCastleProvider.PROVIDER_NAME);
            final PublicKey publicKey = x509Certificate.getPublicKey();
            ContentVerifierProvider contentVerifierProvider = jcaContentVerifierProviderBuilder.build(publicKey);

            if (bor.isSignatureValid(contentVerifierProvider))
                return x509Certificate;
        }
        
        return null;
    }

    /**
     * @see #makeLtvEnabledV1(PdfStamper, OcspClient, CrlClient)
     * @see #makeLtvEnabledV2(PdfStamper, OcspClient, CrlClient)
     */
    static PdfName getOcspSignatureKey(byte[] basicResponseBytes) throws NoSuchAlgorithmException, IOException {
        BasicOCSPResponse basicResponse = BasicOCSPResponse.getInstance(basicResponseBytes);
        byte[] signatureBytes = basicResponse.getSignature().getBytes();
        DEROctetString octetString = new DEROctetString(signatureBytes);
        byte[] octetBytes = octetString.getEncoded();
        byte[] octetHash = hashBytesSha1(octetBytes);
        PdfName octetName = new PdfName(Utilities.convertToHex(octetHash));
        return octetName;
    }

    /**
     * @see #makeLtvEnabledV1(PdfStamper, OcspClient, CrlClient)
     * @see #makeLtvEnabledV2(PdfStamper, OcspClient, CrlClient)
     */
    static PdfName getCrlSignatureKey(byte[] crlBytes) throws NoSuchAlgorithmException, IOException, CRLException, CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509CRL crl = (X509CRL)cf.generateCRL(new ByteArrayInputStream(crlBytes));
        byte[] signatureBytes = crl.getSignature();
        DEROctetString octetString = new DEROctetString(signatureBytes);
        byte[] octetBytes = octetString.getEncoded();
        byte[] octetHash = hashBytesSha1(octetBytes);
        PdfName octetName = new PdfName(Utilities.convertToHex(octetHash));
        return octetName;
    }

    /**
     * @see #makeLtvEnabledV1(PdfStamper, OcspClient, CrlClient)
     * @see #makeLtvEnabledV2(PdfStamper, OcspClient, CrlClient)
     */
    static X509Certificate getIssuerCertificate(X509Certificate certificate) throws IOException, StreamParsingException {
        String url = getCACURL(certificate);
        if (url != null && url.length() > 0) {
            HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
            if (con.getResponseCode() / 100 != 2) {
                throw new IOException(MessageLocalization.getComposedMessage("invalid.http.response.1", con.getResponseCode()));
            }
            //Get Response
            InputStream inp = (InputStream) con.getContent();
            byte[] buf = new byte[1024];
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            while (true) {
                int n = inp.read(buf, 0, buf.length);
                if (n <= 0)
                    break;
                bout.write(buf, 0, n);
            }
            inp.close();
            
            X509CertParser parser = new X509CertParser();
            parser.engineInit(new ByteArrayInputStream(bout.toByteArray()));
            return (X509Certificate) parser.engineRead();

        }
        return null;
    }

    /**
     * @see #makeLtvEnabledV1(PdfStamper, OcspClient, CrlClient)
     * @see #makeLtvEnabledV2(PdfStamper, OcspClient, CrlClient)
     */
    public static String getCACURL(X509Certificate certificate) {
        ASN1Primitive obj;
        try {
            obj = getExtensionValue(certificate, Extension.authorityInfoAccess.getId());
            if (obj == null) {
                return null;
            }
            ASN1Sequence AccessDescriptions = (ASN1Sequence) obj;
            for (int i = 0; i < AccessDescriptions.size(); i++) {
                ASN1Sequence AccessDescription = (ASN1Sequence) AccessDescriptions.getObjectAt(i);
                if ( AccessDescription.size() != 2 ) {
                    continue;
                }
                else if (AccessDescription.getObjectAt(0) instanceof ASN1ObjectIdentifier) {
                    ASN1ObjectIdentifier id = (ASN1ObjectIdentifier)AccessDescription.getObjectAt(0);
                    if ("1.3.6.1.5.5.7.48.2".equals(id.getId())) {
                        ASN1Primitive description = (ASN1Primitive)AccessDescription.getObjectAt(1);
                        String AccessLocation =  getStringFromGeneralName(description);
                        if (AccessLocation == null) {
                            return "" ;
                        }
                        else {
                            return AccessLocation ;
                        }
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * @see #makeLtvEnabledV1(PdfStamper, OcspClient, CrlClient)
     * @see #makeLtvEnabledV2(PdfStamper, OcspClient, CrlClient)
     */
    private static ASN1Primitive getExtensionValue(X509Certificate certificate, String oid) throws IOException {
        byte[] bytes = certificate.getExtensionValue(oid);
        if (bytes == null) {
            return null;
        }
        ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(bytes));
        ASN1OctetString octs = (ASN1OctetString) aIn.readObject();
        aIn = new ASN1InputStream(new ByteArrayInputStream(octs.getOctets()));
        return aIn.readObject();
    }

    /**
     * @see #makeLtvEnabledV1(PdfStamper, OcspClient, CrlClient)
     * @see #makeLtvEnabledV2(PdfStamper, OcspClient, CrlClient)
     */
    private static String getStringFromGeneralName(ASN1Primitive names) throws IOException {
        ASN1TaggedObject taggedObject = (ASN1TaggedObject) names ;
        return new String(ASN1OctetString.getInstance(taggedObject, false).getOctets(), "ISO-8859-1");
    }

    /**
     * @see #makeLtvEnabledV1(PdfStamper, OcspClient, CrlClient)
     * @see #makeLtvEnabledV2(PdfStamper, OcspClient, CrlClient)
     */
    private static byte[] hashBytesSha1(byte[] b) throws NoSuchAlgorithmException {
        MessageDigest sh = MessageDigest.getInstance("SHA1");
        return sh.digest(b);
    }
    
    /**
     * @see #makeLtvEnabledV2(PdfStamper, OcspClient, CrlClient)
     */
    interface VriAdder {
        void accept(Collection<byte[]> ocsps, Collection<byte[]> crls, Collection<byte[]> certs);
    }
}
