package mkl.testarea.signature.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;

import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test is not actually an iText test but uses the example
 * keystore in this project.
 * 
 * @author mkl
 */
public class SignInSteps
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "signature");

    public static final String KEYSTORE = "keystores/demo-rsa2048.ks"; 
    public static final char[] PASSWORD = "demo-rsa2048".toCharArray(); 

    public static KeyStore ks = null;
    public static PrivateKey pk = null;
    public static Certificate[] chain = null;

    @BeforeClass
    public static void setUp() throws Exception
    {
        RESULT_FOLDER.mkdirs();

        BouncyCastleProvider bcp = new BouncyCastleProvider();
        Security.addProvider(bcp);
        //Security.insertProviderAt(bcp, 1);

        ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(KEYSTORE), PASSWORD);
        String alias = (String) ks.aliases().nextElement();
        pk = (PrivateKey) ks.getKey(alias, PASSWORD);
        chain = ks.getCertificateChain(alias);
    }

    /**
     * <a href="http://stackoverflow.com/questions/33305800/difference-between-sha256withrsa-and-sha256-then-rsa">
     * Difference between SHA256withRSA and SHA256 then RSA
     * </a>
     * <p>
     * This method is the original code provided by the OP. As expected it shows two different signatures.
     * </p>
     */
    @Test
    public void testAsGreenhandOriginal() throws GeneralSecurityException, IOException
    {
        System.out.println("\nGreenhandOriginal:");

        String s = "1234";
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(s.getBytes());
        byte[] outputDigest = messageDigest.digest();       
        //sign SHA256 with RSA
        PrivateKey privateKey = pk;//Share.loadPk8("D:/key.pk8");
        Signature rsaSignature = Signature.getInstance("RSA");
        rsaSignature.initSign(privateKey);
        rsaSignature.update(outputDigest);
        byte[] signed = rsaSignature.sign();
        System.out.println(bytesToHex(signed));


        //compute SHA256withRSA as a single step
        Signature rsaSha256Signature = Signature.getInstance("SHA256withRSA");
        rsaSha256Signature.initSign(privateKey);
        rsaSha256Signature.update(s.getBytes());
        byte[] signed2 = rsaSha256Signature.sign();
        System.out.println(bytesToHex(signed2));
    }

    /**
     * <a href="http://stackoverflow.com/questions/33305800/difference-between-sha256withrsa-and-sha256-then-rsa">
     * Difference between SHA256withRSA and SHA256 then RSA
     * </a>
     * <p>
     * This method is the updated code provided by the OP. As expected it shows two equal signatures.
     * The OP's observations seem to differ, though.
     * </p>
     */
    @Test
    public void testAsGreenhandUpdated() throws GeneralSecurityException, IOException
    {
        System.out.println("\nGreenhandUpdated:");

        String s = "1234";
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(s.getBytes());
        byte[] outputDigest = messageDigest.digest();

        AlgorithmIdentifier sha256Aid = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256, null);
        DigestInfo di = new DigestInfo(sha256Aid, outputDigest);
        //sign SHA256 with RSA
        PrivateKey privateKey = pk;//Share.loadPk8("D:/key.pk8");
        Signature rsaSignature = Signature.getInstance("RSA");
        rsaSignature.initSign(privateKey);
        rsaSignature.update(di.toASN1Primitive().getEncoded());
        byte[] signed = rsaSignature.sign();
        System.out.println("method 1: "+bytesToHex(signed));


        //compute SHA256withRSA as a single step
        Signature rsaSha256Signature = Signature.getInstance("SHA256withRSA");
        rsaSha256Signature.initSign(privateKey);
        rsaSha256Signature.update(s.getBytes());
        byte[] signed2 = rsaSha256Signature.sign();
        System.out.println("method 2: "+bytesToHex(signed2));
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
