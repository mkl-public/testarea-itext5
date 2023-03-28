package mkl.testarea.itext5.signature;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.BERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.cms.SignerInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.DefaultAlgorithmNameFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.ByteBuffer;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfString;

/**
 * This tool "fixes" invalid ECDSA signature algorithm OIDs in SignerInfos,
 * in particular it replaces ecPublicKey OIDs there. Beware, such a fix of
 * an inner PDF signature will obviously invalidate the outer signatures.
 * 
 * @author mkl
 */
public class EcdsaAlgorithmCorrector {
    public static void main(String[] args) throws IOException {
        Security.addProvider(new BouncyCastleProvider());
        for (String arg: args) {
            System.out.printf("\n%s\n", arg);
            final File file = new File(arg);
            if (file.exists()) {
                final File result = new File(arg + "-fixed.pdf");
                EcdsaAlgorithmCorrector corrector = new EcdsaAlgorithmCorrector(file, result);
                List<String> signatureNames = corrector.getSignatureNames();
                List<String> fixedSignatures = new ArrayList<String>();
                for (String signatureName : signatureNames) {
                    if (corrector.fix(signatureName))
                        fixedSignatures.add(signatureName);
                }
                if (fixedSignatures.isEmpty()) {
                    System.err.printf("!!! Failed to fix any signature in %s.\n", file);
                } else {
                    System.out.printf("Fixed signatures %s.\n", fixedSignatures);
                }
            } else
                System.err.printf("!!! File does not exist: %s\n", file);
        }
    }

    public EcdsaAlgorithmCorrector(File input, File output) throws IOException {
        FileUtils.copyFile(input, output);
        this.input = input;
        this.output = output;
        pdfReader = new PdfReader(input.getPath());
    }

    public List<String> getSignatureNames() {
        return pdfReader.getAcroFields().getSignatureNames();
    }

    public boolean fix(String signatureName) {
        final AcroFields acroFields = pdfReader.getAcroFields();

        PdfDictionary signatureFieldValue = acroFields.getSignatureDictionary(signatureName);
        if (signatureFieldValue == null) {
            System.out.printf("!!! Signature field '%s' has no value, it is not signed.\n", signatureName);
            return false;
        }

        PdfString contents = signatureFieldValue.getAsString(PdfName.CONTENTS);
        if (contents == null) {
            System.out.printf("!!! Signature field '%s' value has no signature content, it is not signed.\n", signatureName);
            return false;
        }

        byte[] contentBytes = contents.getOriginalBytes();
        CMSSignedData signedData;
        try {
            signedData = new CMSSignedData(contentBytes);
        } catch (CMSException e) {
            System.out.printf("!!! Signature field '%s' value signature content could not be parsed as CMS signature container: %s.\n", signatureName, e.getMessage());
            return false;
        }

        Collection<SignerInfo>  signerInfos = new ArrayList<>();
        boolean fixed = false;
        for (SignerInformation signerInfo : signedData.getSignerInfos().getSigners()) {
            if ("1.2.840.10045.2.1".equals(signerInfo.getEncryptionAlgOID())) {
                String digestAlgorithmName = new DefaultAlgorithmNameFinder().getAlgorithmName(signerInfo.getDigestAlgorithmID());
                AlgorithmIdentifier fixedAlgorithm;
                if (isStandardEncoding(signerInfo.getSignature())) {
                    fixedAlgorithm = new DefaultSignatureAlgorithmIdentifierFinder().find(digestAlgorithmName + "withECDSA");
                } else {
                    fixedAlgorithm = new DefaultSignatureAlgorithmIdentifierFinder().find(digestAlgorithmName + "withPLAIN-ECDSA");
                }
                System.out.printf("ecPublicKey -> %s\n", new DefaultAlgorithmNameFinder().getAlgorithmName(fixedAlgorithm));
                SignerInfo sigInfo = signerInfo.toASN1Structure();
                SignerInfo sigInfoFixed = new SignerInfo(sigInfo.getSID(), sigInfo.getDigestAlgorithm(),
                        sigInfo.getAuthenticatedAttributes(), fixedAlgorithm,
                        sigInfo.getEncryptedDigest(), sigInfo.getUnauthenticatedAttributes());
                signerInfos.add(sigInfoFixed);
                fixed = true;
            } else {
                signerInfos.add(signerInfo.toASN1Structure());
            }
        }
        if (fixed) {
            PdfArray b = signatureFieldValue.getAsArray(PdfName.BYTERANGE);
            long[] gaps = b.asLongArray();
            int spaceAvailable = (int)(gaps[2] - gaps[1]) - 2;

            try {
                signedData = rebuild(signedData, signerInfos);
            } catch (CMSException e) {
                System.out.printf("!!! Signature field '%s' value fixed signature could not be re-nuilt: %s.\n", signatureName, e.getMessage());
                return false;
            }
            try {
                contentBytes = signedData.getEncoded();
            } catch (IOException e) {
                System.out.printf("!!! Signature field '%s' value fixed signature could not be re-encoded: %s.\n", signatureName, e.getMessage());
                return false;
            }
            byte[] dataToWrite;
            try (ByteBuffer bb = new ByteBuffer(spaceAvailable)) {
                for (byte bi : contentBytes) {
                    bb.appendHex(bi);
                }
                int remain = (spaceAvailable - contentBytes.length * 2);
                for (int k = 0; k < remain; ++k) {
                    bb.append((byte)48);
                }
                dataToWrite = bb.toByteArray();
            } catch (IOException e) {
                System.out.printf("!!! Signature field '%s' value fixed signature could not be serialized: %s.\n", signatureName, e.getMessage());
                return false;
            }

            try (RandomAccessFile raFile = new RandomAccessFile(output, "rw")) {
                raFile.seek(gaps[1] + 1);
                raFile.write(dataToWrite);
            } catch (IOException e) {
                System.out.printf("!!! Signature field '%s' value fixed signature could not be written: %s.\n", signatureName, e.getMessage());
                return false;
            }
        }

        return fixed;
    }

    CMSSignedData rebuild(CMSSignedData signedData, Collection<SignerInfo> signerInfos) throws CMSException {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        for (SignerInfo signerInfo : signerInfos) {
            vec.add(signerInfo);
        }
        ASN1Set signers = new DERSet(vec);

        ASN1Sequence sD = (ASN1Sequence)signedData.toASN1Structure().getContent().toASN1Primitive();

        vec = new ASN1EncodableVector();
        for (int i = 0; i < sD.size() - 1; i++) {
            vec.add(sD.getObjectAt(i));
        }
        vec.add(signers);
        SignedData sigData = SignedData.getInstance(new BERSequence(vec));
        ContentInfo contentInfo = new ContentInfo(signedData.toASN1Structure().getContentType(), sigData);
        signedData = new CMSSignedData(contentInfo);

        return signedData;
    }

    boolean isStandardEncoding(byte[] signatureBytes) {
        try {
            ASN1Primitive prim = ASN1Primitive.fromByteArray(signatureBytes);
            if (prim instanceof ASN1Sequence) {
                ASN1Sequence seq = (ASN1Sequence) prim;
                if (seq.size() == 2) {
                    return (seq.getObjectAt(0) instanceof ASN1Integer) && (seq.getObjectAt(1) instanceof ASN1Integer);
                }
            }
        } catch (IOException e) {
            // could not be parsed as BER -> not standard
        }
        return false;
    }

    final File input;
    final File output;
    final PdfReader pdfReader;
}
