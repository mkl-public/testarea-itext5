package mkl.testarea.signature.analyze;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.ocsp.OCSPResponse;
import org.bouncycastle.asn1.ocsp.ResponderID;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.IssuerSerial;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.TBSCertificate;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSVerifierCertificateNotValidException;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.RSAUtil;
import org.bouncycastle.operator.AlgorithmNameFinder;
import org.bouncycastle.operator.DefaultAlgorithmNameFinder;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.RuntimeOperatorException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;


/**
 * This class is meant to eventually become a tool for analyzing signatures.
 * More and more tests shall be added to indicate the issues of the upcoming
 * test signatures.
 * 
 * @author mklink
 */
public class SignatureAnalyzer
{
    private DigestCalculatorProvider digCalcProvider = new BcDigestCalculatorProvider();
    private AlgorithmNameFinder algorithmNameFinder = new DefaultAlgorithmNameFinder();

    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            System.out.printf("\nAnalyzing %s\n", arg);
            byte[] bytes = Files.readAllBytes(FileSystems.getDefault().getPath(arg));
            System.out.print("=========\n");
            new SignatureAnalyzer(bytes);
        }
    }

    public SignatureAnalyzer(byte[] signatureData) throws CMSException, IOException, TSPException, OperatorCreationException, GeneralSecurityException
    {
        signedData = new CMSSignedData(signatureData);
        
        Store<X509CertificateHolder> certificateStore = signedData.getCertificates();
        if (certificateStore == null || certificateStore.getMatches(selectAny).isEmpty())
            System.out.println("\nCertificates: none");
        else
        {
            System.out.println("\nCertificates:");
            for (X509CertificateHolder certificate : (Collection<X509CertificateHolder>) certificateStore.getMatches(selectAny))
            {
                System.out.printf("- Subject: %s\n  Issuer: %s\n  Serial: %s\n", certificate.getSubject(), certificate.getIssuer(), certificate.getSerialNumber());
            }
        }
        
        Store<X509AttributeCertificateHolder> attributeCertificateStore = signedData.getAttributeCertificates();
        if (attributeCertificateStore == null || attributeCertificateStore.getMatches(selectAny).isEmpty())
            System.out.println("\nAttribute Certificates: none");
        else
        {
            System.out.println("\nAttribute Certificates: TODO!!!");
        }
        
        Store<X509CRLHolder> crls = signedData.getCRLs();
        if (crls == null || crls.getMatches(selectAny).isEmpty())
            System.out.println("\nCRLs: none");
        else
        {
            System.out.println("\nCRLs: TODO!!!");
        }

        for (SignerInformation signerInfo : (Collection<SignerInformation>)signedData.getSignerInfos().getSigners())
        {
            System.out.printf("\nSignerInfo: %s / %s\n", signerInfo.getSID().getIssuer(), signerInfo.getSID().getSerialNumber());

            Store<X509CertificateHolder> certificates = signedData.getCertificates();
            Collection<X509CertificateHolder> certs = certificates.getMatches(signerInfo.getSID());
            
            System.out.print("Certificate: ");
            X509CertificateHolder cert = null;
            if (certs.size() != 1)
            {
                System.out.printf("Could not identify, %s candidates\n", certs.size());
            }
            else
            {
                cert = certs.iterator().next();
                System.out.printf("%s\n", cert.getSubject());
            }

            if (signerInfo.getSignedAttributes() == null) {
                System.out.println("!!! No signed attributes");
                analyzeSignatureBytes(signerInfo.getSignature(), cert, null);
                continue;
            }

            Map<ASN1ObjectIdentifier, ?> attributes = signerInfo.getSignedAttributes().toHashtable();

            for (Map.Entry<ASN1ObjectIdentifier, ?> attributeEntry : attributes.entrySet())
            {
                System.out.printf("Signed attribute %s", attributeEntry.getKey());
                
                if (attributeEntry.getKey().equals(ADBE_REVOCATION_INFO_ARCHIVAL))
                {
                    System.out.println(" (Adobe Revocation Information Archival)");
                    Attribute attribute = (Attribute) attributeEntry.getValue();
                    
                    for (ASN1Encodable encodable : attribute.getAttrValues().toArray())
                    {
                        ASN1Sequence asn1Sequence = (ASN1Sequence) encodable;
                        for (ASN1Encodable taggedEncodable : asn1Sequence.toArray())
                        {
                            ASN1TaggedObject asn1TaggedObject = (ASN1TaggedObject) taggedEncodable; 
                            switch (asn1TaggedObject.getTagNo())
                            {
                            case 0:
                            {
                                ASN1Sequence crlSeq = (ASN1Sequence) asn1TaggedObject.getObject();
                                for (ASN1Encodable crlEncodable : crlSeq.toArray())
                                {
                                    System.out.println(" CRL " + crlEncodable.getClass());
                                }

                                break;
                            }
                            case 1:
                            {
                                ASN1Sequence ocspSeq = (ASN1Sequence) asn1TaggedObject.getObject();
                                for (ASN1Encodable ocspEncodable : ocspSeq.toArray())
                                {
                                    OCSPResponse ocspResponse = OCSPResponse.getInstance(ocspEncodable);
                                    OCSPResp ocspResp = new OCSPResp(ocspResponse);
                                    int status = ocspResp.getStatus();
                                    BasicOCSPResp basicOCSPResp;
                                    try
                                    {
                                        basicOCSPResp = (BasicOCSPResp) ocspResp.getResponseObject();
                                        System.out.printf(" OCSP Response status %s - %s - %s\n", status, basicOCSPResp.getProducedAt(), ((ResponderID)basicOCSPResp.getResponderId().toASN1Primitive()).getName());
                                        for (X509CertificateHolder certificate : basicOCSPResp.getCerts())
                                        {
                                            System.out.printf("  Cert w/ Subject: %s\n          Issuer: %s\n           Serial: %s\n", certificate.getSubject(), certificate.getIssuer(), certificate.getSerialNumber());
                                        }
                                        for (SingleResp singleResp : basicOCSPResp.getResponses())
                                        {
                                            System.out.printf("  Response %s for ", singleResp.getCertStatus());
                                            X509CertificateHolder issuer = null;
                                            for (X509CertificateHolder certificate : basicOCSPResp.getCerts())
                                            {
                                                if (singleResp.getCertID().matchesIssuer(certificate, digCalcProvider))
                                                    issuer = certificate;
                                            }
                                            if (issuer == null)
                                            {
                                                System.out.printf("Serial %s and (hash algorithm %s) name %s / key %s\n", singleResp.getCertID().getSerialNumber(), singleResp.getCertID().getHashAlgOID(), toHex(singleResp.getCertID().getIssuerNameHash()), toHex(singleResp.getCertID().getIssuerKeyHash()));
                                            }
                                            else
                                            {
                                                System.out.printf("Issuer: %s Serial: %s\n", issuer.getSubject(), singleResp.getCertID().getSerialNumber());
                                            }
                                        }
                                    }
                                    catch (OCSPException e)
                                    {
                                        System.out.printf(" !! Failure parsing OCSP response object: %s\n", e.getMessage());
                                    }
                                }
                                break;
                            }
                            case 2:
                            {
                                ASN1Sequence otherSeq = (ASN1Sequence) asn1TaggedObject.getObject();
                                for (ASN1Encodable otherEncodable : otherSeq.toArray())
                                {
                                    System.out.println(" Other " + otherEncodable.getClass());
                                }
                                break;
                            }
                            default:
                                break;
                            }
                        }
                    }
                }
                else if (attributeEntry.getKey().equals(PKCSObjectIdentifiers.pkcs_9_at_contentType))
                {
                    System.out.println(" (PKCS 9 - Content Type)");
                }
                else if (attributeEntry.getKey().equals(PKCSObjectIdentifiers.pkcs_9_at_messageDigest))
                {
                    System.out.println(" (PKCS 9 - Message Digest)");
                    Attribute attribute = (Attribute) attributeEntry.getValue();
                    ASN1Encodable[] values = attribute.getAttributeValues();
                    if (values == null || values.length == 0)
                        System.out.println("!!! No Message Digest value");
                    else {
                        if (values.length > 1)
                            System.out.println("!!! Multiple Message Digest values");
                        for (ASN1Encodable value : values) {
                            if (value instanceof ASN1OctetString) {
                                byte[] octets = ((ASN1OctetString)value).getOctets();
                                System.out.printf("Digest: %s\n", toHex(octets));
                                try {
                                    Field resultDigestField = signerInfo.getClass().getDeclaredField("resultDigest");
                                    resultDigestField.setAccessible(true);
                                    resultDigestField.set(signerInfo, octets);
                                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                                    System.out.println("## Introspection failure: " + e.getMessage());
                                }

                            } else
                                System.out.println("!!! Invalid Message Digest value type " + value.getClass());
                        }
                    }
                }
                else if (attributeEntry.getKey().equals(PKCSObjectIdentifiers.id_aa_signingCertificateV2))
                {
                    System.out.println(" (Signing certificate v2)");
                    Attribute attribute = (Attribute) attributeEntry.getValue();
                    ASN1Encodable[] values = attribute.getAttributeValues();
                    if (values == null || values.length == 0)
                        System.out.println("!!! No signing certificate v2 value");
                    else {
                        if (values.length > 1)
                            System.out.println("!!! Multiple signing certificate v2 values");
                        for (ASN1Encodable value : values) {
                            if (value instanceof ASN1Sequence) {
                                ASN1Sequence certIds = (ASN1Sequence) ((ASN1Sequence) value).getObjectAt(0);
                                for (ASN1Encodable certId : certIds) {
                                    ASN1Sequence certIdSeq = (ASN1Sequence) certId;
                                    int issuerSerialPosition = 1;
                                    if (certIdSeq.getObjectAt(0) instanceof ASN1OctetString) {
                                        System.out.println("    " + NISTObjectIdentifiers.id_sha256 + " (SHA256) by default");
                                    } else {
                                        issuerSerialPosition = 2;
                                        AlgorithmIdentifier algorithm = AlgorithmIdentifier.getInstance(certIdSeq.getObjectAt(0).toASN1Primitive());
                                        System.out.println("    " + algorithm.getAlgorithm() + " (" + algorithmNameFinder.getAlgorithmName(algorithm) + ")");
                                        if (NISTObjectIdentifiers.id_sha256.equals(algorithm.getAlgorithm())) {
                                            System.out.println("!!! Default algorithm explicitly serialized --> not DER");
                                        }
                                    }
                                    if (certIdSeq.size() > issuerSerialPosition) {
                                        System.out.println("    " + IssuerSerial.getInstance(certIdSeq.getObjectAt(issuerSerialPosition)));
                                    }
                                }
                            } else
                                System.out.println("!!! Invalid signing certificate v2 value type " + value.getClass());
                        }
                    }
                }
                else
                {
                    System.out.println();
                }

                System.out.println();
            }

            byte[] signedAttributeBytes = signerInfo.getEncodedSignedAttributes();
            MessageDigest md = MessageDigest.getInstance(signerInfo.getDigestAlgOID());
            byte[] signedAttributeHash = md.digest(signedAttributeBytes);
            String signedAttributeHashString = toHex(signedAttributeHash);
            System.out.printf("Signed Attributes Hash: %s\n", signedAttributeHashString);
            md.reset();
            byte[] signedAttributeHashHash = md.digest(signedAttributeHash);
            String signedAttributeHashHashString = toHex(signedAttributeHashHash);
            System.out.printf("Signed Attributes Hash Hash: %s\n", signedAttributeHashHashString);

            // earlier BC versions returned getEncodedSignedAttributes in their original encoding
            // for a DER encoding mismatch test, the explicit DER encoding needed to be tested
            byte[] derSignedAttributeBytes = new DERSet(ASN1Set.getInstance(signedAttributeBytes).toArray()).getEncoded(ASN1Encoding.DER);
            if (!Arrays.equals(derSignedAttributeBytes, signedAttributeBytes)) {
                System.out.println("!!! Signed attribute bytes not DER encoded (1)");
                md.reset();
                byte[] derSignedAttributeHash = md.digest(derSignedAttributeBytes);
                String derSignedAttributeHashString = toHex(derSignedAttributeHash);
                System.out.printf("DER Signed Attributes Hash: %s\n", derSignedAttributeHashString);
                Files.write(Paths.get("C:\\Temp\\1.ber"), signedAttributeBytes);
                Files.write(Paths.get("C:\\Temp\\1.der"), derSignedAttributeBytes);
            }

            // newer BC versions return getEncodedSignedAttributes in DER encoding
            // for a DER encoding mismatch test, the original encoding needs to be tested
            byte[] origSignedAttributeBytes = signerInfo.toASN1Structure().getAuthenticatedAttributes().getEncoded();
            if (!Arrays.equals(origSignedAttributeBytes, signedAttributeBytes)) {
                System.out.println("!!! Signed attribute bytes not DER encoded (2)");
                md.reset();
                byte[] origSignedAttributeHash = md.digest(origSignedAttributeBytes);
                String origSignedAttributeHashString = toHex(origSignedAttributeHash);
                System.out.printf("Original Signed Attributes Hash: %s\n", origSignedAttributeHashString);
                Files.write(Paths.get("C:\\Temp\\1.ber"), origSignedAttributeBytes);
                Files.write(Paths.get("C:\\Temp\\1.der"), signedAttributeBytes);
            }

            if (cert != null) {
                byte[] digestBytes = analyzeSignatureBytes(signerInfo.getSignature(), cert, derSignedAttributeBytes);
                if (digestBytes != null) {
                    String digestString = toHex(digestBytes);
                    if (!digestString.equals(signedAttributeHashString)) {
                        System.out.println("!!! Decrypted RSA signature with PKCS1 1.5 padding does not contain signed attributes hash");
                        if (digestString.equals(signedAttributeHashHashString))
                            System.out.println("!!! but it contains the hash of the signed attributes hash");
                    }
                }

                System.out.println();

                try {
                    if(signerInfo.verify(new JcaSimpleSignerInfoVerifierBuilder().build(cert)))
                        System.out.println("Signature validates with certificate");
                    else
                        System.out.println("!!! Signature does not validate with certificate");
                } catch(CMSVerifierCertificateNotValidException e) {
                    System.out.println("!!! Certificate not valid at claimed signing time: " + e.getMessage());
                } catch(CertificateException e) {
                    System.out.println("!!! Verification failure (Certificate): " + e.getMessage());
                } catch(IllegalArgumentException e) {
                    System.out.println("!!! Verification failure (Illegal argument): " + e.getMessage());
                } catch(RuntimeOperatorException e) {
                    System.out.println("!!! Verification failure (Runtime Operator): " + e.getMessage());
                } catch(CMSException e2) {
                    System.out.println("!!! Verification failure: " + e2.getMessage());
                }

                System.out.println("\nCertificate path from accompanying certificates");
                X509CertificateHolder c = cert;
                JcaContentVerifierProviderBuilder jcaContentVerifierProviderBuilder = new JcaContentVerifierProviderBuilder();
                while (c != null) {
                    System.out.printf("- %s ", c.getSubject());
                    X500Name issuer = c.getIssuer();
                    Collection<X509CertificateHolder> cs = certificates.getMatches(new Selector<X509CertificateHolder>() {
                        @Override
                        public boolean match(X509CertificateHolder obj) {
                            return  obj.getSubject().equals(issuer);
                        }

                        @Override
                        public Object clone() {
                            return this;
                        }
                    });
                    if (cs.size() != 1) {
                        System.out.printf("(no unique match - %d)", cs.size());
                        break;
                    }
                    X509CertificateHolder cc = cs.iterator().next();
                    try {
                        boolean isValid = c.isSignatureValid(jcaContentVerifierProviderBuilder.build(cc));
                        System.out.print(isValid ? "(valid signature)" : "(invalid signature)");
                        TBSCertificate tbsCert = c.toASN1Structure().getTBSCertificate();
                        try (   ByteArrayOutputStream sOut = new ByteArrayOutputStream() ) {
                            ASN1OutputStream dOut = ASN1OutputStream.create(sOut, ASN1Encoding.DER);
                            dOut.writeObject(tbsCert);
                            byte[] tbsDerBytes = sOut.toByteArray();
                            boolean isDer = Arrays.equals(tbsDerBytes, tbsCert.getEncoded());
                            if (!isDer)
                                System.out.print(" (TBSCertificate not DER encoded)");
                        }
                        if (!isValid) {
                            System.out.println("\nHashes of the TBSCertificate:");
                            for (Map.Entry<String, MessageDigest> entry : SignatureAnalyzer.digestByName.entrySet()) {
                                String digestName = entry.getKey();
                                MessageDigest digest = entry.getValue();
                                digest.reset();
                                byte[] digestValue = digest.digest(tbsCert.getEncoded());
                                System.out.printf(" * %s: %s\n", digestName, SignatureAnalyzer.toHex(digestValue));
                            }
                            analyzeSignatureBytes(c.getSignature(), cc, null);
                        }
                    } catch (CertException | CertificateException e) {
                        System.out.printf("(inappropriate signature - %s)", e.getMessage());
                    }
                    if (c == cc) {
                        System.out.print(" (self-signed)");
                        c = null;
                    } else
                        c = cc;
                    System.out.println();
                }
            }

            System.out.println();

            if (certificates != null) {
                for (Object certObject : certificates.getMatches(selectAny)) {
                    X509CertificateHolder certHolder = (X509CertificateHolder) certObject;
                    try {
                        boolean verify = signerInfo.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certHolder));
                        System.out.printf("Verify %s with '%s'.\n", verify ? "succeeds" : "fails", certHolder.getSubject());
                    } catch(Exception ex) {
                        System.out.printf("Verify throws exception with '%s': '%s'.\n", certHolder.getSubject(), ex.getMessage());
                    }
                }
            }

            System.out.println();

            AttributeTable attributeTable = signerInfo.getUnsignedAttributes();
            if (attributeTable != null)
            {
                attributes = attributeTable.toHashtable();
                
                for (Map.Entry<ASN1ObjectIdentifier, ?> attributeEntry : attributes.entrySet())
                {
                    System.out.printf("Unsigned attribute %s", attributeEntry.getKey());
                    
                    if (attributeEntry.getKey().equals(/*SIGNATURE_TIME_STAMP_OID*/PKCSObjectIdentifiers.id_aa_signatureTimeStampToken))
                    {
                        System.out.println(" (Signature Time Stamp)");
                        Attribute attribute = (Attribute) attributeEntry.getValue();
                        
                        for (ASN1Encodable encodable : attribute.getAttrValues().toArray())
                        {
                            ContentInfo contentInfo = ContentInfo.getInstance(encodable);
                            TimeStampToken timeStampToken = new TimeStampToken(contentInfo);
                            TimeStampTokenInfo tstInfo = timeStampToken.getTimeStampInfo();

                            System.out.printf("Authority/SN %s / %s\n", tstInfo.getTsa(), tstInfo.getSerialNumber());
                            
                            DigestCalculator digCalc = digCalcProvider .get(tstInfo.getHashAlgorithm());

                            OutputStream dOut = digCalc.getOutputStream();

                            dOut.write(signerInfo.getSignature());
                            dOut.close();

                            byte[] expectedDigest = digCalc.getDigest();
                            boolean matches =  Arrays.equals(expectedDigest, tstInfo.getMessageImprintDigest());
                            
                            System.out.printf("Digest match? %s\n", matches);

                            System.out.printf("Signer %s / %s\n", timeStampToken.getSID().getIssuer(), timeStampToken.getSID().getSerialNumber());
                            
                            Store<X509CertificateHolder> tstCertificates = timeStampToken.getCertificates();
                            Collection tstCerts = tstCertificates.getMatches(new SignerId(timeStampToken.getSID().getIssuer(), timeStampToken.getSID().getSerialNumber()));
                            
                            System.out.print("Certificate: ");
                            
                            if (tstCerts.size() != 1)
                            {
                                System.out.printf("Could not identify, %s candidates\n", tstCerts.size());
                            }
                            else
                            {
                                X509CertificateHolder tstCert = (X509CertificateHolder) tstCerts.iterator().next();
                                System.out.printf("%s\n", tstCert.getSubject());
                                
                                int version = tstCert.toASN1Structure().getVersionNumber();
                                System.out.printf("Version: %s\n", version);
                                if (version != 3)
                                    System.out.println("Error: Certificate must be version 3 to have an ExtendedKeyUsage extension.");
                                
                                Extension ext = tstCert.getExtension(Extension.extendedKeyUsage);
                                if (ext == null)
                                    System.out.println("Error: Certificate must have an ExtendedKeyUsage extension.");
                                else
                                {
                                    if (!ext.isCritical())
                                    {
                                        System.out.println("Error: Certificate must have an ExtendedKeyUsage extension marked as critical.");
                                    }
                                    
                                    ExtendedKeyUsage    extKey = ExtendedKeyUsage.getInstance(ext.getParsedValue());
                                    if (!extKey.hasKeyPurposeId(KeyPurposeId.id_kp_timeStamping) || extKey.size() != 1)
                                    {
                                        System.out.println("Error: ExtendedKeyUsage not solely time stamping.");
                                    }                             
                                }
                            }
                        }
                    }
                    else
                        System.out.println();
                }
            }
        } 
    }

    public static byte[] analyzeSignatureBytes(byte[] signatureBytes, X509CertificateHolder cert, byte[] signedData) throws GeneralSecurityException, IOException {
        if (RSAUtil.isRsaOid(cert.getSubjectPublicKeyInfo().getAlgorithm().getAlgorithm())) {
            KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = rsaKeyFactory.generatePublic(new X509EncodedKeySpec(cert.getSubjectPublicKeyInfo().getEncoded()));

            try {
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, publicKey);
                byte[] bytes = cipher.doFinal(signatureBytes);
                System.out.printf("Decrypted signature bytes: %s\n", toHex(bytes));
                try {
                    DigestInfo digestInfo = DigestInfo.getInstance(bytes);
                    String digestString = toHex(digestInfo.getDigest());
                    System.out.printf("Decrypted signature digest algorithm: %s\n", digestInfo.getAlgorithmId().getAlgorithm());
                    System.out.printf("Decrypted signature digest: %s\n", digestString);

                    if (signedData != null) {
                        MessageDigest digest = MessageDigest.getInstance(digestInfo.getAlgorithmId().getAlgorithm().toString());
                        byte[] actualDigest = digest.digest(signedData);
                        if (!Arrays.equals(actualDigest, digestInfo.getDigest())) {
                            String actualDigestString = toHex(actualDigest);
                            System.out.printf("Actual signed data digest: %s\n", actualDigestString);
                        } else {
                            System.out.println("Decrypted signature digest matches signed data digest.");
                        }
                    }

                    return digestInfo.getDigest();
                } catch (IllegalArgumentException iae) {
                    System.out.println("!!! Decrypted, PKCS1 padded RSA signature is not well-formed: " + iae.getMessage());
                }
            } catch (BadPaddingException bpe) {
                System.out.println("!!! Decrypted RSA signature is not PKCS1 padded: " + bpe.getMessage());
                try {
                    Cipher cipherNoPadding = Cipher.getInstance("RSA/ECB/NoPadding");
                    cipherNoPadding.init(Cipher.DECRYPT_MODE, publicKey);
                    byte[] bytes = cipherNoPadding.doFinal(signatureBytes);
                    System.out.printf("Decrypted signature bytes: %s\n", toHex(bytes));
                    if (bytes[bytes.length - 1] != (byte) 0xbc)
                        System.out.println("!!! Decrypted RSA signature does not end with the PSS 0xbc byte either");
                    else {
                        System.out.println("Decrypted RSA signature does end with the PSS 0xbc byte");
                    }
                } catch(BadPaddingException bpe2) {
                    System.out.println("!!! Failure decrypted RSA signature: " + bpe2.getMessage());
                }
            } catch (IllegalBlockSizeException ibse) {
                System.out.println("!!! Signature size does not match key size: " + ibse.getMessage());
            }
        }
        return null;
    }

    public static String toHex(byte[] bytes)
    {
        if (bytes == null)
            return "null";

        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ )
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    final Selector selectAny = new Selector()
    {
        @Override
        public boolean match(Object obj)
        {
            return true;
        }

        @Override
        public Object clone()
        {
            return this;
        }
    };

    final static List<String> digestNames = Arrays.asList("SHA-512", "SHA-384", "SHA-256", "SHA-224", "SHA1");
    public final static Map<String, MessageDigest> digestByName = new LinkedHashMap<>();

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

    static final ASN1ObjectIdentifier adobe = new ASN1ObjectIdentifier("1.2.840.113583");
    static final ASN1ObjectIdentifier acrobat = adobe.branch("1");
    static final ASN1ObjectIdentifier security = acrobat.branch("1");
    static final ASN1ObjectIdentifier ADBE_REVOCATION_INFO_ARCHIVAL = security.branch("8");
    
    final CMSSignedData signedData;
    final static ASN1ObjectIdentifier SIGNATURE_TIME_STAMP_OID = new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.2.14");
}
