package mkl.testarea.signature.analyze;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

import org.bouncycastle.cert.X509CertificateHolder;

/**
 * This class uses a {@link SignatureAnalyzer} method to decrypt a
 * RSA signature and extract the signed hash if possible.
 * 
 * @author mkl
 */
public class SignatureBytesAnalyzer {

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        if (args.length == 2) {
            byte[] certificateBytes = Files.readAllBytes(Paths.get(args[0]));
            X509CertificateHolder certificate = new X509CertificateHolder(certificateBytes);
            byte[] signatureBytes = Files.readAllBytes(Paths.get(args[1]));

            byte[] digestBytes = SignatureAnalyzer.analyzeSignatureBytes(signatureBytes, certificate);

            if (digestBytes != null) {
                System.out.print("Decimal signature digest bytes:");
                for (byte b : digestBytes) {
                    System.out.print(' ');
                    System.out.print(((int) b) & 255);
                }
            }
        }
    }
}
