package mkl.testarea.itext5.form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.io.StreamUtil;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfAppearance;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PushbuttonField;
import com.itextpdf.text.pdf.XfaForm;

import mkl.testarea.itext5.signature.CreateSignature;

/**
 * @author mkl
 */
public class SetImageInSignedPdf {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/52220196/itext5-x-setting-pushbutton-appearance-without-breaking-seal">
     * iText5.x Setting pushbutton appearance without breaking Seal
     * </a>
     * <br/>
     * <a href="http://www.filedropper.com/samplecert">
     * sampleCert.pdf
     * </a>
     * <p>
     * This is the OPs original code. Indeed, <code>setField</code> breaks the seal.
     * </p>
     */
    @Test
    public void testSetLikeBro1SampleCert() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("sampleCert.pdf");
                InputStream image = getClass().getResourceAsStream("/mkl/testarea/itext5/layer/Willi-1.jpg");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "sampleCert-image-1.pdf"))   ) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result, pdfReader.getPdfVersion(), true);

            AcroFields acroFields = pdfStamper.getAcroFields();
            String imageFieldId = "mainform[0].subform_0[0].image_0_0[0]";
            acroFields.setField(imageFieldId, Base64.getEncoder().encodeToString(StreamUtil.inputStreamToArray(image)));

            pdfStamper.close();
            pdfReader.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/52220196/itext5-x-setting-pushbutton-appearance-without-breaking-seal">
     * iText5.x Setting pushbutton appearance without breaking Seal
     * </a>
     * <br/>
     * <a href="http://www.filedropper.com/samplecert">
     * sampleCert.pdf
     * </a>, cleared from signature, reduced to approval, resigned as sampleSig.pdf
     * <p>
     * This is the OPs original code applied to his document with the certification
     * replaced by an approval. Indeed, <code>setField</code> does not breaks this
     * signature.
     * </p>
     * @see CreateSignature#signDeferredSampleSigClean()
     */
    @Test
    public void testSetLikeBro1SampleSig() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("sampleSig.pdf");
                InputStream image = getClass().getResourceAsStream("/mkl/testarea/itext5/layer/Willi-1.jpg");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "sampleSig-image-1.pdf"))   ) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result, pdfReader.getPdfVersion(), true);

            AcroFields acroFields = pdfStamper.getAcroFields();
            String imageFieldId = "mainform[0].subform_0[0].image_0_0[0]";
            acroFields.setField(imageFieldId, Base64.getEncoder().encodeToString(StreamUtil.inputStreamToArray(image)));

            pdfStamper.close();
            pdfReader.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/52220196/itext5-x-setting-pushbutton-appearance-without-breaking-seal">
     * iText5.x Setting pushbutton appearance without breaking Seal
     * </a>
     * <br/>
     * <a href="http://www.filedropper.com/samplecert">
     * sampleCert.pdf
     * </a>
     * <p>
     * This is the OPs original code. Indeed, explicit Button replacement breaks the seal.
     * </p>
     */
    @Test
    public void testSetLikeBro2SampleCert() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("sampleCert.pdf");
                InputStream image = getClass().getResourceAsStream("/mkl/testarea/itext5/layer/Willi-1.jpg");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "sampleCert-image-2.pdf"))   ) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result, pdfReader.getPdfVersion(), true);

            AcroFields acroFields = pdfStamper.getAcroFields();
            String imageFieldId = "mainform[0].subform_0[0].image_0_0[0]";
            PushbuttonField pbField = acroFields.getNewPushbuttonFromField(imageFieldId);
            pbField.setImage(Image.getInstance(StreamUtil.inputStreamToArray(image)));
            acroFields.replacePushbuttonField(imageFieldId, pbField.getField());

            pdfStamper.close();
            pdfReader.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/52220196/itext5-x-setting-pushbutton-appearance-without-breaking-seal">
     * iText5.x Setting pushbutton appearance without breaking Seal
     * </a>
     * <br/>
     * <a href="http://www.filedropper.com/samplecert">
     * sampleCert.pdf
     * </a>
     * <p>
     * This method only manipulates the XFA form. This code leaves the seal valid.
     * </p>
     */
    @Test
    public void testSetInXfaOnlySampleCert() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("sampleCert.pdf");
                InputStream image = getClass().getResourceAsStream("/mkl/testarea/itext5/layer/Willi-1.jpg");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "sampleCert-image-xfaOnly.pdf"))   ) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result, pdfReader.getPdfVersion(), true);

            AcroFields acroFields = pdfStamper.getAcroFields();
            String name = "mainform[0].subform_0[0].image_0_0[0]";
            String value = Base64.getEncoder().encodeToString(StreamUtil.inputStreamToArray(image));

            XfaForm xfa = acroFields.getXfa();
            if (xfa.isXfaPresent()) {
                name = xfa.findFieldName(name, acroFields);
                if (name != null) {
                    String shortName = XfaForm.Xml2Som.getShortName(name);
                    Node xn = xfa.findDatasetsNode(shortName);
                    if (xn == null) {
                        xn = xfa.getDatasetsSom().insertNode(xfa.getDatasetsNode(), shortName);
                    }
                    xfa.setNodeText(xn, value);
                }
            }

            pdfStamper.close();
            pdfReader.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/52220196/itext5-x-setting-pushbutton-appearance-without-breaking-seal">
     * iText5.x Setting pushbutton appearance without breaking Seal
     * </a>
     * <br/>
     * <a href="http://www.filedropper.com/samplecert">
     * sampleCert.pdf
     * </a>
     * <p>
     * This method only manipulates the AcroForm button appearance.
     * This code leaves the seal valid.
     * </p>
     */
    @Test
    public void testSetInAppearanceOnlySampleCert() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("sampleCert.pdf");
                InputStream extra = getClass().getResourceAsStream("/mkl/testarea/itext5/layer/Willi-1.jpg");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "sampleCert-image-appearanceOnly.pdf"))   ) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result, pdfReader.getPdfVersion(), true);
            Image image = Image.getInstance(StreamUtil.inputStreamToArray(extra));
            
            AcroFields acroFields = pdfStamper.getAcroFields();
            String name = "mainform[0].subform_0[0].image_0_0[0]";
            PdfDictionary widget = acroFields.getFieldItem(name).getWidget(0);
            PdfArray boxArray = widget.getAsArray(PdfName.RECT);
            Rectangle box = new Rectangle(boxArray.getAsNumber(0).floatValue(), boxArray.getAsNumber(1).floatValue(), boxArray.getAsNumber(2).floatValue(), boxArray.getAsNumber(3).floatValue());

            float ratioImage = image.getWidth() / image.getHeight();
            float ratioBox = box.getWidth() / box.getHeight();
            boolean fillHorizontally = ratioImage > ratioBox;
            float width = fillHorizontally ? 1 : ratioBox / ratioImage;
            float height = fillHorizontally ? ratioImage / ratioBox : 1;
            float xOffset = 0; // centered: (width - 1) / 2;
            float yOffset = height - 1; // centered: (height - 1) / 2;
            PdfAppearance app = PdfAppearance.createAppearance(pdfStamper.getWriter(), width, height);
            app.addImage(image, 1, 0, 0, 1, xOffset, yOffset);
            PdfDictionary dic = (PdfDictionary)widget.get(PdfName.AP);
            if (dic == null)
                dic = new PdfDictionary();
            dic.put(PdfAnnotation.APPEARANCE_NORMAL, app.getIndirectReference());
            widget.put(PdfName.AP, dic);
            pdfStamper.markUsed(widget);

            pdfStamper.close();
            pdfReader.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/52220196/itext5-x-setting-pushbutton-appearance-without-breaking-seal">
     * iText5.x Setting pushbutton appearance without breaking Seal
     * </a>
     * <br/>
     * <a href="http://www.filedropper.com/samplecert">
     * sampleCert.pdf
     * </a>
     * <p>
     * This method manipulates the XFA form and the AcroForm button appearance.
     * This code leaves the seal valid.
     * </p>
     */
    @Test
    public void testSetInXfaAndAppearanceSampleCert() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("sampleCert.pdf");
                InputStream extra = getClass().getResourceAsStream("/mkl/testarea/itext5/layer/Willi-1.jpg");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "sampleCert-image-xfaAppearance.pdf"))   ) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result, pdfReader.getPdfVersion(), true);
            byte[] bytes = StreamUtil.inputStreamToArray(extra);

            AcroFields acroFields = pdfStamper.getAcroFields();
            String name = "mainform[0].subform_0[0].image_0_0[0]";
            String value = Base64.getEncoder().encodeToString(bytes);
            Image image = Image.getInstance(bytes);

            XfaForm xfa = acroFields.getXfa();
            if (xfa.isXfaPresent()) {
                name = xfa.findFieldName(name, acroFields);
                if (name != null) {
                    String shortName = XfaForm.Xml2Som.getShortName(name);
                    Node xn = xfa.findDatasetsNode(shortName);
                    if (xn == null) {
                        xn = xfa.getDatasetsSom().insertNode(xfa.getDatasetsNode(), shortName);
                    }
                    xfa.setNodeText(xn, value);
                }
            }

            PdfDictionary widget = acroFields.getFieldItem(name).getWidget(0);
            PdfArray boxArray = widget.getAsArray(PdfName.RECT);
            Rectangle box = new Rectangle(boxArray.getAsNumber(0).floatValue(), boxArray.getAsNumber(1).floatValue(), boxArray.getAsNumber(2).floatValue(), boxArray.getAsNumber(3).floatValue());

            float ratioImage = image.getWidth() / image.getHeight();
            float ratioBox = box.getWidth() / box.getHeight();
            boolean fillHorizontally = ratioImage > ratioBox;
            float width = fillHorizontally ? 1 : ratioBox / ratioImage;
            float height = fillHorizontally ? ratioImage / ratioBox : 1;
            float xOffset = 0; // centered: (width - 1) / 2;
            float yOffset = height - 1; // centered: (height - 1) / 2;
            PdfAppearance app = PdfAppearance.createAppearance(pdfStamper.getWriter(), width, height);
            app.addImage(image, 1, 0, 0, 1, xOffset, yOffset);
            PdfDictionary dic = (PdfDictionary)widget.get(PdfName.AP);
            if (dic == null)
                dic = new PdfDictionary();
            dic.put(PdfAnnotation.APPEARANCE_NORMAL, app.getIndirectReference());
            widget.put(PdfName.AP, dic);
            pdfStamper.markUsed(widget);

            pdfStamper.close();
            pdfReader.close();
        }
    }
}
