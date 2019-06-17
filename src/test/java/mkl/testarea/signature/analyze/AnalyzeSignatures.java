package mkl.testarea.signature.analyze;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPException;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class AnalyzeSignatures
{
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }

    /**
     * <a href="http://stackoverflow.com/questions/34544380/pdf-signing-with-timestamp-certificate-details-does-not-appear-in-timestamp-pro">
     * PDF signing with timestamp: certificate details does not appear in timestamp properties
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B61KdyZ43x-9cEk5YVV6WVd4WUU/view?usp=sharing">
     * PDFSigned.pdf
     * </a>,
     * the signature being extracted as "PDFSigned.pdf.Signature1.raw".
     * 
     * <p>
     * The OP used the same certificate for signing the signature and the time stamp, it
     * in particular does not have the required extended key usage marking it as a time
     * stamping certificate.
     * </p>
     */
    @Test
    public void testAGDevSignatureWithTimeStamp() throws IOException, CMSException, TSPException, OperatorCreationException, GeneralSecurityException
    {
        try (InputStream resource = getClass().getResourceAsStream("PDFSigned.pdf.Signature1.raw"))
        {
            byte[] signatureBytes = IOUtils.toByteArray(resource);
            
            SignatureAnalyzer analyzer = new SignatureAnalyzer(signatureBytes);
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/35613203/pdf-signature-ltv-crl-alternative">
     * PDF Signature - LTV - CRL alternative?
     * </a>
     * <br/>
     * <a href="http://we.tl/dBFE114SAd">
     * test_signed.pdf
     * </a>,
     * the signature being extracted as "test_signed.pdf.Signature1.raw".
     * 
     * <p>
     * The signature does not conform to any LTV profile, merely to T-Level, i.e. it is timestamped.
     * </p>
     */
    @Test
    public void testTonnySignature() throws IOException, CMSException, TSPException, OperatorCreationException, GeneralSecurityException
    {
        try (InputStream resource = getClass().getResourceAsStream("test_signed.pdf.Signature1.raw"))
        {
            byte[] signatureBytes = IOUtils.toByteArray(resource);
            
            SignatureAnalyzer analyzer = new SignatureAnalyzer(signatureBytes);
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/40979157/how-to-detect-a-signed-pdf-is-valid-with-itext">
     * How to detect a signed pdf is valid with iText?
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B8fLGZLVFcLyeXF0TjluNzRjT3c/view?usp=sharing">
     * corrupted-sign-file.pdf
     * </a>,
     * the signature being extracted as "corrupted-sign-file.pdf.Signature2.raw".
     * 
     * <p>
     * It does not become clear here why Adobe Reader rejects the signature.
     * </p>
     */
    @Test
    public void testKe20Signature() throws IOException, CMSException, TSPException, OperatorCreationException, GeneralSecurityException
    {
        try (InputStream resource = getClass().getResourceAsStream("corrupted-sign-file.pdf.Signature2.raw"))
        {
            byte[] signatureBytes = IOUtils.toByteArray(resource);
            
            SignatureAnalyzer analyzer = new SignatureAnalyzer(signatureBytes);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/51031446/c-sharp-pkcs7-smartcard-digital-signature-document-has-been-altered-or-corrupt">
     * C# PKCS7 Smartcard Digital Signature - Document has been altered or corrupted since it was signed
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1I6hUO8B3fnkgws9Pk1Puti9HjkRJE0Rq/view?usp=sharing">
     * signedpdf.pdf
     * </a>,
     * the signature being extracted as "signedpdf.pdf.dsa.raw".
     * 
     * <p>
     * As it turns out, the signature DigestInfo object contains the
     * hash of the hash of the signed attributes, not simply the
     * hash of the signed attributes, i.e. the signed attributes
     * incorrectly are hashed twice.
     * </p>
     */
    @Test
    public void testSotnSignature() throws IOException, CMSException, TSPException, OperatorCreationException, GeneralSecurityException
    {
        try (InputStream resource = getClass().getResourceAsStream("signedpdf.pdf.dsa.raw"))
        {
            byte[] signatureBytes = IOUtils.toByteArray(resource);
            
            SignatureAnalyzer analyzer = new SignatureAnalyzer(signatureBytes);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/54361974/how-to-compute-pdf-signature-hash">
     * How to compute PDF signature hash?
     * </a>
     * <br/>
     * <a href="https://gist.githubusercontent.com/nowox/94dd54e484df877e1232c18bd7b91c97/raw/d249f3757137e9b665e895c900f08b1156f1bc4f/dummy-signed.pdf.base64">
     * dummy-signed.pdf
     * </a>,
     * the signature being extracted as "dummy-signed.pdf.Signature2.raw".
     * <p>
     * As it turns out, the OP looked at the wrong hash value (not the hash
     * value in the messageDigest signed attribute but the encrypted hash
     * value in the signature bytes). The value in the messageDigest signed
     * attribute coincides with the hash of the signed byte ranges of the
     * example PDF.
     * </p>
     */
    @Test
    public void testNowoxSignature() throws IOException, CMSException, TSPException, OperatorCreationException, GeneralSecurityException
    {
        try (InputStream resource = getClass().getResourceAsStream("dummy-signed.pdf.Signature2.raw"))
        {
            byte[] signatureBytes = IOUtils.toByteArray(resource);
            
            SignatureAnalyzer analyzer = new SignatureAnalyzer(signatureBytes);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/54339668/how-to-itextsharp-5-5-sign-hash-that-was-generated-by-java-itext-5-5">
     * how to : iTextSharp 5.5 sign hash that was generated by Java iText 5.5
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/eozsjoebv5fc2nt/test-signed.pdf?dl=0">
     * test-signed.pdf
     * </a>,
     * the signature being extracted as "test-signed.pdf.DSE_signer1548305268021.raw".
     * <p>
     * The signature bytes in the signature container in the example PDF are
     * not created by RSA-signing (neither a EMSA-PKCS1-v1_5 padding nor a
     * trailing 0xbc as in EMSA-PSS) as is with the alleged signer certificate
     * "C=MO,O=Macao Post and Telecommunications Bureau,OU=Government Qualified Certificate G03,
     * OU=Terms of use at www.esigntrust.com/CPS,OU=DEPT/ORG - Direccao dos Servicos de Economia,
     * OU=UNIT - N/A,OU=PROCUR - N/A,SERIALNUMBER=0000001762,T=Tecnico Superior Assessor,
     * CN=Hoi Ka CHAO,E=eric@economia.gov.mo".
     * </p>
     */
    @Test
    public void testEricMacauSignature() throws IOException, CMSException, TSPException, OperatorCreationException, GeneralSecurityException
    {
        try (InputStream resource = getClass().getResourceAsStream("test-signed.pdf.DSE_signer1548305268021.raw"))
        {
            byte[] signatureBytes = IOUtils.toByteArray(resource);
            
            SignatureAnalyzer analyzer = new SignatureAnalyzer(signatureBytes);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/54124067/how-to-sign-a-pdf-with-itextsharp-using-usbkey">
     * How to sign a pdf with itextsharp using USBKey?
     * </a>
     * <br/>
     * <a href="https://share.weiyun.com/5l7nKzQ">
     * Hello_world2.pdf
     * </a>,
     * the signature being extracted as "Hello_world2.pdf.itextSharp.raw".
     * <p>
     * The signature bytes appear not to be created using the
     * alleged signing certificate.
     * </p>
     */
    @Test
    public void testJinSignatureHelloWorld2() throws IOException, CMSException, TSPException, OperatorCreationException, GeneralSecurityException
    {
        try (InputStream resource = getClass().getResourceAsStream("Hello_world2.pdf.itextSharp.raw"))
        {
            byte[] signatureBytes = IOUtils.toByteArray(resource);
            
            SignatureAnalyzer analyzer = new SignatureAnalyzer(signatureBytes);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/54124067/how-to-sign-a-pdf-with-itextsharp-using-usbkey">
     * How to sign a pdf with itextsharp using USBKey?
     * </a>
     * <br/>
     * <a href="https://share.weiyun.com/5iOd6CQ">
     * Hello_world3.pdf
     * </a>,
     * the signature being extracted as "Hello_world3.pdf.itextSharp.raw".
     * <p>
     * The signature bytes appear not to be created using the
     * alleged signing certificate.
     * </p>
     */
    @Test
    public void testJinSignatureHelloWorld3() throws IOException, CMSException, TSPException, OperatorCreationException, GeneralSecurityException
    {
        try (InputStream resource = getClass().getResourceAsStream("Hello_world3.pdf.itextSharp.raw"))
        {
            byte[] signatureBytes = IOUtils.toByteArray(resource);
            
            SignatureAnalyzer analyzer = new SignatureAnalyzer(signatureBytes);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/54559547/external-signing-pdf-with-itext">
     * External signing PDF with iText
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1PD0avPqV-Qj6tNWQ_ifGRmGFBeLDadSj/view?usp=sharing">
     * temp_scap.pdf
     * </a>,
     * the signature being extracted as "temp_scap.pdf.SignatureTeste0.raw".
     * <p>
     * Due to multiple errors in the signature generation process,
     * this signature cannot be sensibly validated.
     * </p>
     */
    @Test
    public void testGonçaloGrazinaSignatureTempScap() throws IOException, CMSException, TSPException, OperatorCreationException, GeneralSecurityException
    {
        try (InputStream resource = getClass().getResourceAsStream("temp_scap.pdf.SignatureTeste0.raw"))
        {
            byte[] signatureBytes = IOUtils.toByteArray(resource);
            
            SignatureAnalyzer analyzer = new SignatureAnalyzer(signatureBytes);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/54559547/external-signing-pdf-with-itext">
     * External signing PDF with iText
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1PD0avPqV-Qj6tNWQ_ifGRmGFBeLDadSj/view?usp=sharing">
     * signedFile.pdf
     * </a>,
     * the signature being extracted as "signedFile.pdf.SignatureTeste0.raw".
     * <p>
     * One issue remains: the decrypted, de-padded value of the signature bytes
     * is the naked hash, not a DigestInfo structure for the hash.
     * </p>
     */
    @Test
    public void testGonçaloGrazinaSignatureSignedFile() throws IOException, CMSException, TSPException, OperatorCreationException, GeneralSecurityException
    {
        try (InputStream resource = getClass().getResourceAsStream("signedFile.pdf.SignatureTeste0.raw"))
        {
            byte[] signatureBytes = IOUtils.toByteArray(resource);
            
            SignatureAnalyzer analyzer = new SignatureAnalyzer(signatureBytes);
        }
    }

    /**
     * <a href="https://ec.europa.eu/cefdigital/tracker/browse/DSS-1538">
     * Cannot analyze signatures Exception Error
     * </a>
     * <br/>
     * <a href="https://ec.europa.eu/cefdigital/tracker/secure/attachment/18729/vypis_z_kn.pdf">
     * vypis_z_kn.pdf
     * </a>,
     * the signature being extracted as "vypis_z_kn.pdf.Signature1.raw".
     * <p>
     * There is no issue validating the signature. As it turns out the problem
     * was that the PDF is encrypted and the signature value dictionary does
     * not have a type entry. Thus, PDFBox does not recognize the unencrypted
     * signature container as such and attempts to decrypt it, scrambling it
     * in the course of that.
     * </p>
     */
    @Test
    public void testJurajZacekSignatureVypis_z_kn() throws IOException, CMSException, TSPException, OperatorCreationException, GeneralSecurityException
    {
        try (InputStream resource = getClass().getResourceAsStream("vypis_z_kn.pdf.Signature1.raw"))
        {
            byte[] signatureBytes = IOUtils.toByteArray(resource);
            
            SignatureAnalyzer analyzer = new SignatureAnalyzer(signatureBytes);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/55042503/sign-pdf-hash-using-java-and-itext">
     * Sign PDF hash using java and iText
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/13wpirorpEnj2dKi7ZTo76pYhx9Hfiz5v/view?usp=sharing">
     * sampleinformedconsent_signed.pdf
     * </a>,
     * the signature being extracted as "sampleinformedconsent_signed.pdf.Signature1.raw".
     * <p>
     * The signed hash is neither the hash of the authenticated attributes
     * nor that of the signed byte ranges. Cannot help yet.
     * </p>
     */
    @Test
    public void testPriyankaSignatureSampleinformedconsent_Signed() throws IOException, CMSException, TSPException, OperatorCreationException, GeneralSecurityException
    {
        try (InputStream resource = getClass().getResourceAsStream("sampleinformedconsent_signed.pdf.Signature1.raw"))
        {
            byte[] signatureBytes = IOUtils.toByteArray(resource);
            
            SignatureAnalyzer analyzer = new SignatureAnalyzer(signatureBytes);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/55901977/insert-a-signedhash-into-pdf-for-external-signing-process-workingsample">
     * Insert a SignedHash into PDF for external signing process - WorkingSample
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/sh/wxt7ksghv34it1y/AABYLHYr85OW1QBKYy6m5h6ra?dl=0">
     * signed.pdf
     * </a>,
     * the signature being extracted as "signed.pdf.sig.raw".
     * <p>
     * The signed hash is neither the hash of the authenticated attributes
     * nor that of the signed byte ranges. Cannot help yet.
     * </p>
     */
    @Test
    public void testFabrizioBaroneSignatureSigned() throws IOException, CMSException, TSPException, OperatorCreationException, GeneralSecurityException
    {
        try (InputStream resource = getClass().getResourceAsStream("signed.pdf.sig.raw"))
        {
            byte[] signatureBytes = IOUtils.toByteArray(resource);
            
            SignatureAnalyzer analyzer = new SignatureAnalyzer(signatureBytes);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/56560783/makesignature-signdeferred-embedding-signature-problem-itextsharp">
     * MakeSignature.SignDeferred & Embedding Signature Problem (Itextsharp)
     * </a>
     * <br/>
     * <a href="http://kurumsalhizmet.hobim.com/misc">
     * test_signed.pdf
     * </a>,
     * the signature being extracted as "test_signed.pdf.SIG.raw".
     * <p>
     * The signature bytes are neither PKCS1-v1_5 padded nor
     * EMSA-PSS encoded. Something already gone wrong in
     * communication with the signature card. Probably the
     * wrong signing app there-on was addressed.
     * </p>
     */
    @Test
    public void testDenizKasarSignatureTestSigned() throws IOException, CMSException, TSPException, OperatorCreationException, GeneralSecurityException
    {
        try (InputStream resource = getClass().getResourceAsStream("test_signed.pdf.SIG.raw"))
        {
            byte[] signatureBytes = IOUtils.toByteArray(resource);
            
            SignatureAnalyzer analyzer = new SignatureAnalyzer(signatureBytes);
        }
    }
}
