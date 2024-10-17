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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.X509CertParser;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OperatorException;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.util.Store;
import org.bouncycastle.x509.util.StreamParsingException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.error_messages.MessageLocalization;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDeveloperExtension;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfIndirectReference;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfStream;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.security.CrlClient;
import com.itextpdf.text.pdf.security.CrlClientOnline;
import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.OcspClientBouncyCastle;
import com.itextpdf.text.pdf.security.PdfPKCS7;

/**
 * <a href="https://stackoverflow.com/questions/51370965/how-can-i-add-pades-ltv-using-itext">
 * how can I add PAdES-LTV using itext
 * </a>
 * <p>
 * This class adds LTV information to a signed PDF to make it LTV enabled
 * as reported by Adobe Acrobat.
 * </p>
 * 
 * @author mkl
 */
public class AdobeLtvEnabling {
    public static void main(String[] args) throws IOException, DocumentException, OperatorException, GeneralSecurityException, StreamParsingException, OCSPException, CMSException {
        Security.addProvider(new BouncyCastleProvider());
        for (String arg: args) {
            System.out.printf("\n%s\n", arg);
            final File file = new File(arg);
            if (file.exists()) {
                try (OutputStream result = new FileOutputStream(new File(arg + "-ltv.pdf"))) {
                    PdfReader pdfReader = new PdfReader(file.getAbsolutePath());
                    PdfStamper pdfStamper = new PdfStamper(pdfReader, result, (char)0, true);

                    AdobeLtvEnabling adobeLtvEnabling = new AdobeLtvEnabling(pdfStamper);
                    OcspClient ocsp = new OcspClientBouncyCastle();
                    CrlClient crl = new CrlClientOnline();
                    adobeLtvEnabling.enable(ocsp, crl);

                    pdfStamper.close();
                }
            } else
                System.err.printf("!!! File does not exist: %s\n", file);
        }
    }

    /**
     * Use this constructor with a {@link PdfStamper} in append mode. Otherwise
     * the existing signatures will be damaged.
     */
    public AdobeLtvEnabling(PdfStamper pdfStamper) {
        this.pdfStamper = pdfStamper;
    }

    /**
     * Call this method to have LTV information added to the {@link PdfStamper}
     * given in the constructor.
     */
    public void enable(OcspClient ocspClient, CrlClient crlClient) throws OperatorException, GeneralSecurityException, IOException, StreamParsingException, OCSPException, CMSException {
        AcroFields fields = pdfStamper.getAcroFields();
        boolean encrypted = pdfStamper.getReader().isEncrypted();

        ArrayList<String> names = fields.getSignatureNames();
        for (String name : names)
        {
            /*
            PdfPKCS7 pdfPKCS7 = fields.verifySignature(name);
            PdfDictionary signatureDictionary = fields.getSignatureDictionary(name);
            List<X509Certificate> certificatesToCheck = new ArrayList<>();
            certificatesToCheck.add(pdfPKCS7.getSigningCertificate());
            while (!certificatesToCheck.isEmpty()) {
                X509Certificate certificate = certificatesToCheck.remove(0);
                addLtvForChain(certificate, ocspClient, crlClient, getSignatureHashKey(signatureDictionary, encrypted));
            }
             */
            JcaX509CertificateConverter converter = new JcaX509CertificateConverter().setProvider("BC");
            PdfDictionary signatureDictionary = fields.getSignatureDictionary(name);
            PdfString contents = signatureDictionary.getAsString(PdfName.CONTENTS);
            CMSSignedData signedData = new CMSSignedData(contents.getOriginalBytes());
            Store certs = signedData.getCertificates();
            for (Object signerInformationObject : signedData.getSignerInfos().getSigners()) {
                SignerInformation signerInformation = (SignerInformation) signerInformationObject;
                Collection signerCerts = certs.getMatches(signerInformation.getSID());
                for (Object certObject : signerCerts) {
                    X509CertificateHolder certHolder = (X509CertificateHolder) certObject;
                    addLtvForChain(converter.getCertificate(certHolder), ocspClient, crlClient, getSignatureHashKey(signatureDictionary, encrypted));
                }
            }
        }

        outputDss();
    }

    //
    // the actual LTV enabling methods
    //
    void addLtvForChain(X509Certificate certificate, OcspClient ocspClient, CrlClient crlClient, PdfName key) throws GeneralSecurityException, IOException, StreamParsingException, OperatorCreationException, OCSPException {
        ValidationData validationData = new ValidationData();

        while (certificate != null) {
            System.out.println(certificate.getSubjectX500Principal().getName());
            X509Certificate issuer = getIssuerCertificate(certificate);
            validationData.certs.add(certificate.getEncoded());
            byte[] ocspResponse = ocspClient.getEncoded(certificate, issuer, null);
            if (ocspResponse != null) {
                System.out.println("  with OCSP response");
                validationData.ocsps.add(ocspResponse);
                X509Certificate ocspSigner = getOcspSignerCertificate(ocspResponse);
                if (ocspSigner != null) {
                    System.out.printf("  signed by %s\n", ocspSigner.getSubjectX500Principal().getName());
                }
                addLtvForChain(ocspSigner, ocspClient, crlClient, getOcspHashKey(ocspResponse));
            } else {
               Collection<byte[]> crl = crlClient.getEncoded(certificate, null);
               if (crl != null && !crl.isEmpty()) {
                   System.out.printf("  with %s CRLs\n", crl.size());
                   validationData.crls.addAll(crl);
                   for (byte[] crlBytes : crl) {
                       addLtvForChain(null, ocspClient, crlClient, getCrlHashKey(crlBytes));
                   }
               }
            }
            certificate = issuer;
        }

        validated.put(key, validationData);
    }

    void outputDss() throws IOException {
        PdfWriter writer = pdfStamper.getWriter();
        PdfReader reader = pdfStamper.getReader();

        PdfDictionary dss = new PdfDictionary();
        PdfDictionary vrim = new PdfDictionary();
        PdfArray ocsps = new PdfArray();
        PdfArray crls = new PdfArray();
        PdfArray certs = new PdfArray();

        writer.addDeveloperExtension(PdfDeveloperExtension.ESIC_1_7_EXTENSIONLEVEL5);
        writer.addDeveloperExtension(new PdfDeveloperExtension(PdfName.ADBE, new PdfName("1.7"), 8));

        PdfDictionary catalog = reader.getCatalog();
        pdfStamper.markUsed(catalog);
        for (PdfName vkey : validated.keySet()) {
            PdfArray ocsp = new PdfArray();
            PdfArray crl = new PdfArray();
            PdfArray cert = new PdfArray();
            PdfDictionary vri = new PdfDictionary();
            for (byte[] b : validated.get(vkey).crls) {
                PdfStream ps = new PdfStream(b);
                ps.flateCompress();
                PdfIndirectReference iref = writer.addToBody(ps, false).getIndirectReference();
                crl.add(iref);
                crls.add(iref);
            }
            for (byte[] b : validated.get(vkey).ocsps) {
                b = buildOCSPResponse(b);
                PdfStream ps = new PdfStream(b);
                ps.flateCompress();
                PdfIndirectReference iref = writer.addToBody(ps, false).getIndirectReference();
                ocsp.add(iref);
                ocsps.add(iref);
            }
            for (byte[] b : validated.get(vkey).certs) {
                PdfStream ps = new PdfStream(b);
                ps.flateCompress();
                PdfIndirectReference iref = writer.addToBody(ps, false).getIndirectReference();
                cert.add(iref);
                certs.add(iref);
            }
            if (ocsp.size() > 0)
                vri.put(PdfName.OCSP, writer.addToBody(ocsp, false).getIndirectReference());
            if (crl.size() > 0)
                vri.put(PdfName.CRL, writer.addToBody(crl, false).getIndirectReference());
            if (cert.size() > 0)
                vri.put(PdfName.CERT, writer.addToBody(cert, false).getIndirectReference());
            vri.put(PdfName.TU, new PdfDate());
            vrim.put(vkey, writer.addToBody(vri, false).getIndirectReference());
        }
        dss.put(PdfName.VRI, writer.addToBody(vrim, false).getIndirectReference());
        if (ocsps.size() > 0)
            dss.put(PdfName.OCSPS, writer.addToBody(ocsps, false).getIndirectReference());
        if (crls.size() > 0)
            dss.put(PdfName.CRLS, writer.addToBody(crls, false).getIndirectReference());
        if (certs.size() > 0)
            dss.put(PdfName.CERTS, writer.addToBody(certs, false).getIndirectReference());
        catalog.put(PdfName.DSS, writer.addToBody(dss, false).getIndirectReference());
    }

    //
    // VRI signature hash key calculation
    //
    static PdfName getCrlHashKey(byte[] crlBytes) throws NoSuchAlgorithmException, IOException, CRLException, CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509CRL crl = (X509CRL)cf.generateCRL(new ByteArrayInputStream(crlBytes));
        byte[] signatureBytes = crl.getSignature();
        DEROctetString octetString = new DEROctetString(signatureBytes);
        byte[] octetBytes = octetString.getEncoded();
        byte[] octetHash = hashBytesSha1(octetBytes);
        PdfName octetName = new PdfName(Utilities.convertToHex(octetHash));
        return octetName;
    }

    static PdfName getOcspHashKey(byte[] basicResponseBytes) throws NoSuchAlgorithmException, IOException {
        BasicOCSPResponse basicResponse = BasicOCSPResponse.getInstance(basicResponseBytes);
        byte[] signatureBytes = basicResponse.getSignature().getBytes();
        DEROctetString octetString = new DEROctetString(signatureBytes);
        byte[] octetBytes = octetString.getEncoded();
        byte[] octetHash = hashBytesSha1(octetBytes);
        PdfName octetName = new PdfName(Utilities.convertToHex(octetHash));
        return octetName;
    }

    static PdfName getSignatureHashKey(PdfDictionary dic, boolean encrypted) throws NoSuchAlgorithmException, IOException {
        PdfString contents = dic.getAsString(PdfName.CONTENTS);
        byte[] bc = contents.getOriginalBytes();
        if (PdfName.ETSI_RFC3161.equals(PdfReader.getPdfObject(dic.get(PdfName.SUBFILTER)))) {
            try (   ASN1InputStream din = new ASN1InputStream(new ByteArrayInputStream(bc)) ) {
                ASN1Primitive pkcs = din.readObject();
                bc = pkcs.getEncoded();
            }
        }
        byte[] bt = hashBytesSha1(bc);
        return new PdfName(Utilities.convertToHex(bt));
    }

    static byte[] hashBytesSha1(byte[] b) throws NoSuchAlgorithmException {
        MessageDigest sh = MessageDigest.getInstance("SHA1");
        return sh.digest(b);
    }

    //
    // OCSP response helpers
    //
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

    static byte[] buildOCSPResponse(byte[] BasicOCSPResponse) throws IOException {
        DEROctetString doctet = new DEROctetString(BasicOCSPResponse);
        ASN1EncodableVector v2 = new ASN1EncodableVector();
        v2.add(OCSPObjectIdentifiers.id_pkix_ocsp_basic);
        v2.add(doctet);
        ASN1Enumerated den = new ASN1Enumerated(0);
        ASN1EncodableVector v3 = new ASN1EncodableVector();
        v3.add(den);
        v3.add(new DERTaggedObject(true, 0, new DERSequence(v2)));            
        DERSequence seq = new DERSequence(v3);
        return seq.getEncoded();
    }

    //
    // X509 certificate related helpers
    //
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

    static String getCACURL(X509Certificate certificate) {
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

    static ASN1Primitive getExtensionValue(X509Certificate certificate, String oid) throws IOException {
        byte[] bytes = certificate.getExtensionValue(oid);
        if (bytes == null) {
            return null;
        }
        ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(bytes));
        ASN1OctetString octs = (ASN1OctetString) aIn.readObject();
        aIn = new ASN1InputStream(new ByteArrayInputStream(octs.getOctets()));
        return aIn.readObject();
    }

    private static String getStringFromGeneralName(ASN1Primitive names) throws IOException {
        ASN1TaggedObject taggedObject = (ASN1TaggedObject) names ;
        return new String(ASN1OctetString.getInstance(taggedObject, false).getOctets(), "ISO-8859-1");
    }

    //
    // inner class
    //
    static class ValidationData {
        final List<byte[]> crls = new ArrayList<byte[]>();
        final List<byte[]> ocsps = new ArrayList<byte[]>();
        final List<byte[]> certs = new ArrayList<byte[]>();
    }

    //
    // member variables
    //
    final PdfStamper pdfStamper;

    final Map<PdfName,ValidationData> validated = new HashMap<PdfName,ValidationData>();
}
