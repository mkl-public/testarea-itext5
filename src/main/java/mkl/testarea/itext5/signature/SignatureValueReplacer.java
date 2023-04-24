package mkl.testarea.itext5.signature;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.BEROctetString;
import org.bouncycastle.asn1.BERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.cms.SignerInfo;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.ByteBuffer;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfString;

/**
 * This tool replaces a given signature value in a PDF by another value.
 * 
 * @author mkl
 */
public class SignatureValueReplacer {
    public static void main(String[] args) throws IOException {
        Security.addProvider(new BouncyCastleProvider());
        if (args.length < 3) {
            System.out.print("Usage: SignatureValueReplacer <original PDF> <original value> <replacement value> [<replacement PDF>]");
        } else {
            File pdfIn = new File(args[0]);
            byte[] original = Files.readAllBytes(Paths.get(args[1]));
            byte[] replacement = Files.readAllBytes(Paths.get(args[2]));
            File pdfOut = new File(args.length == 3 ? args[0] + "-replaced.pdf" : args[3]);
            SignatureValueReplacer corrector = new SignatureValueReplacer(pdfIn, pdfOut);
            List<String> signatureNames = corrector.getSignatureNames();
            List<String> fixedSignatures = new ArrayList<String>();
            for (String signatureName : signatureNames) {
                if (corrector.fix(signatureName, original, replacement))
                    fixedSignatures.add(signatureName);
            }
            if (fixedSignatures.isEmpty()) {
                System.err.printf("!!! Failed to fix any signature in %s.\n", pdfIn);
            } else {
                System.out.printf("Fixed signatures %s.\n", fixedSignatures);
            }
        }
    }

    public SignatureValueReplacer(File input, File output) throws IOException {
        FileUtils.copyFile(input, output);
        this.input = input;
        this.output = output;
        pdfReader = new PdfReader(input.getPath());
    }

    public List<String> getSignatureNames() {
        return pdfReader.getAcroFields().getSignatureNames();
    }

    public boolean fix(String signatureName, byte[] original, byte[] replacement) {
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
            if (Arrays.equals(original, signerInfo.getSignature())) {
                SignerInfo sigInfo = signerInfo.toASN1Structure();
                SignerInfo sigInfoFixed = new SignerInfo(sigInfo.getSID(), sigInfo.getDigestAlgorithm(),
                        sigInfo.getAuthenticatedAttributes(), sigInfo.getDigestEncryptionAlgorithm(),
                        new BEROctetString(replacement), sigInfo.getUnauthenticatedAttributes());
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
                System.out.printf("!!! Signature field '%s' value fixed signature could not be re-built: %s.\n", signatureName, e.getMessage());
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
