package mkl.testarea.itext5.signature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Arrays;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;

/**
 * @author mkl
 */
public class PdfTestSigner {
    public static final String KEYSTORE = "keystores/demo-rsa2048.p12"; 
    public static final char[] PASSWORD = "demo-rsa2048".toCharArray(); 

    public static KeyStore ks = null;
    public static PrivateKey pk = null;
    public static Certificate[] chain = null;

    static {
        BouncyCastleProvider bcp = new BouncyCastleProvider();
        Security.insertProviderAt(bcp, 1);

        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(new FileInputStream(KEYSTORE), PASSWORD);
            String alias = (String) ks.aliases().nextElement();
            pk = (PrivateKey) ks.getKey(alias, PASSWORD);
            chain = ks.getCertificateChain(alias);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, DocumentException, GeneralSecurityException {
        CryptoStandard cryptoStandard = CryptoStandard.CMS;
        for (String arg: args) {
            if (Arrays.stream(CryptoStandard.values()).anyMatch(c -> c.name().equals(arg))) {
                cryptoStandard = CryptoStandard.valueOf(arg);
                System.out.printf("\n### Selected crypto standard %s.\n\n", cryptoStandard);
            } else {
                System.out.printf("***\n*** %s\n***\n\n", arg);
                final File file = new File(arg);
                if (file.exists()) {
                    File target = new File(file.getParent(), file.getName() + "-signed-" + cryptoStandard + ".pdf");
                    new PdfTestSigner(file, cryptoStandard).sign(target);
                    System.out.println("   signed successfully.\n");
                } else
                    System.err.println("!!! File does not exist: " + file);
            }
        }
    }

    final File file;
    final CryptoStandard cryptoStandard;

    public PdfTestSigner(File file, CryptoStandard cryptoStandard) {
        this.file = file;
        this.cryptoStandard = cryptoStandard;
    }

    public void sign(File target) throws IOException, DocumentException, GeneralSecurityException {
        PdfReader reader = new PdfReader(file.getAbsolutePath());
        try (   FileOutputStream os = new FileOutputStream(target)  ) {
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', null, true);
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "Signature");
            ExternalDigest digest = new BouncyCastleDigest();
            ExternalSignature signature = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, null);
            MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, cryptoStandard);
        }
    }
}
