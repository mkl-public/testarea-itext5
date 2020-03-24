package mkl.testarea.itext5.signature;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPException;

import com.itextpdf.text.io.RASInputStream;
import com.itextpdf.text.io.RandomAccessSource;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.io.StreamUtil;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfString;

import mkl.testarea.signature.analyze.SignatureAnalyzer;

/**
 * This tool analyzes embedded PDF signatures. For the analysis of
 * the contained CMS container it makes use of the PDF agnostic
 * {@link SignatureAnalyzer} class.
 * 
 * @author mkl
 */
public class PdfSignatureAnalyzer {
    public static void main(String[] args) throws IOException, OperatorCreationException, CMSException, TSPException, GeneralSecurityException {
        for (String arg: args)
        {
            System.out.printf("***\n*** %s\n***\n\n", arg);
            final File file = new File(arg);
            if (file.exists()) {
                PdfReader globalReader = new PdfReader(file.getAbsolutePath());
                final AcroFields acroFields = globalReader.getAcroFields();
                System.out.printf("Signature fields with value: %s\n\n", acroFields.getSignatureNames());
                for (String name: acroFields.getSignatureNames()) {
                    System.out.printf("+++\n+++ %s\n+++\n\n", name);
                    try (   InputStream revision = acroFields.extractRevision(name) ) {
                        PdfReader reader = new PdfReader(revision);
                        new PdfSignatureAnalyzer(reader, name);
                    }
                }
            } else
                System.err.println("!!! File does not exist: " + file);
        }
    }

    public PdfSignatureAnalyzer(PdfReader reader, String signatureFieldName) throws IOException, OperatorCreationException, CMSException, TSPException, GeneralSecurityException {
        final AcroFields acroFields = reader.getAcroFields();

        PdfDictionary signatureFieldValue = acroFields.getSignatureDictionary(signatureFieldName);
        if (signatureFieldValue == null) {
            System.out.printf("!!! Signature field '%s' has no value, it is not signed.\n", signatureFieldName);
            return;
        }

        System.out.printf("Subfilter: %s\n", signatureFieldValue.getAsName(PdfName.SUBFILTER));
        PdfArray byteRangeArray = signatureFieldValue.getAsArray(PdfName.BYTERANGE);
        System.out.printf("Byte ranges: %s\n", byteRangeArray);
        long[] gaps = byteRangeArray.asLongArray();
        RandomAccessSource readerSource = reader.getSafeFile().createSourceView();
        final byte[] signedBytes;
        try (InputStream rangeStream = new RASInputStream(new RandomAccessSourceFactory().createRanged(readerSource, gaps))) {
            signedBytes = StreamUtil.inputStreamToArray(rangeStream);
        }

        System.out.println("\nHashes of the signed bytes:");
        for (Map.Entry<String, MessageDigest> entry : digestByName.entrySet()) {
            String digestName = entry.getKey();
            MessageDigest digest = entry.getValue();
            digest.reset();
            byte[] digestValue = digest.digest(signedBytes);
            System.out.printf(" * %s: %s\n", digestName, SignatureAnalyzer.toHex(digestValue));
        }
        System.out.println();

        PdfString contents = signatureFieldValue.getAsString(PdfName.CONTENTS);
        if (contents != null) {
            byte[] contentBytes = contents.getOriginalBytes();
            if (byteRangeArray.size() != 4 || gaps[0] != 0 || gaps[1] > gaps[2]) {
                System.out.printf("!!! Signature byte ranges '%s' are non-conformant. Skipping content-gap test.\n", byteRangeArray);
            } else {
                long[] innerGap = new long[] {gaps[1], gaps[2]-gaps[1]};
                final byte[] innerBytes;
                try (InputStream innerStream = new RASInputStream(new RandomAccessSourceFactory().createRanged(readerSource, innerGap))) {
                    innerBytes = StreamUtil.inputStreamToArray(innerStream);
                }
                String innerString = new String(innerBytes, "US-ASCII");
                if (innerString.length() < 2) {
                    System.out.printf("!!! Signature byte ranges gap too small: '%s'.\n", innerString);
                } else if (!(innerString.startsWith("<") && innerString.endsWith(">"))) {
                    System.out.printf("!!! Signature byte ranges gap is not envelopped in '<' and '>' as a hex encoded String: '%s'.\n", innerString);
                } else {
                    String contentsHex = SignatureAnalyzer.toHex(contentBytes);
                    String innerStringHex = innerString.substring(1, innerString.length() - 1);
                    if (contentsHex.equalsIgnoreCase(innerStringHex)) {
                        System.out.println("Signature byte ranges gap is the hex encoded signature contents value.");
                    } else {
                        System.out.printf("!!! Signature byte ranges gap is not the hex encoded signature contents value:\nContents: %s\nGap:      %s\n", contentsHex, innerStringHex);
                    }
                }
            }
            new SignatureAnalyzer(contentBytes);
        } else
            System.out.printf("!!! Signature field '%s' value has no contents, it is not signed.\n", signatureFieldName);
    }

    final static List<String> digestNames = Arrays.asList("SHA-512", "SHA-384", "SHA-256", "SHA-224", "SHA1");
    final static Map<String, MessageDigest> digestByName = new LinkedHashMap<>();

    static {
        for (String name : digestNames) {
            try {
                MessageDigest digest = MessageDigest.getInstance(name);
                digestByName.put(name, digest);
            } catch (NoSuchAlgorithmException e) {
                System.err.printf("Unknown digest algorithm '%s'. Skipping.\n", name);
            }
        }
        System.err.println();
    }

}
