package mkl.testarea.signature.analyze;

import java.io.IOException;
import java.io.InputStream;

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
    public void testAGDevSignatureWithTimeStamp() throws IOException, CMSException, TSPException, OperatorCreationException
    {
        try (InputStream resource = getClass().getResourceAsStream("PDFSigned.pdf.Signature1.raw"))
        {
            byte[] signatureBytes = IOUtils.toByteArray(resource);
            
            SignatureAnalyzer analyzer = new SignatureAnalyzer(signatureBytes);
        }
    }
}
