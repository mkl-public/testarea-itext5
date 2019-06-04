package mkl.testarea.itext5.annotate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfAppearance;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfBorderDictionary;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfRectangle;
import com.itextpdf.text.pdf.PdfStamper;

public class CloudBoxAnnotation {
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/56388137/box-cloud-annotation-appearance-in-itext">
     * Box Cloud Annotation Appearance in iText
     * </a>
     * <p>
     * Indeed, this code creates a broken visualization. But the created
     * content stream is already invalid, with arbitrary instructions
     * between path definition and path drawing, and furthermore there is
     * the instruction <code>app.rectangle(cborder.getBBox())</code>
     * essentially doing nothing which surely is not meant like this.
     * </p>
     * @see #testDrawLikeChitgoksImproved()
     */
    @Test
    public void testDrawLikeChitgoks() throws IOException, DocumentException {
        PdfReader reader = new PdfReader(getClass().getResourceAsStream("file.pdf"));
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, "CloudBoxLikeChitgoks.pdf")));

        PdfDictionary be = new PdfDictionary();
        be.put(PdfName.S, PdfName.C);
        be.put(PdfName.I, new PdfNumber(1));

        Rectangle location = new Rectangle(123.6f, 584.4f, 252.6f, 653.4f);
        PdfAnnotation stamp = PdfAnnotation.createSquareCircle(stamper.getWriter(), location, "", true);
        stamp.setBorderStyle(new PdfBorderDictionary(1, PdfBorderDictionary.STYLE_SOLID));
        stamp.put(new PdfName("BE"), be);
        stamp.setColor(BaseColor.RED);

        PdfContentByte cb = stamper.getOverContent(1);
        PdfAppearance app = cb.createAppearance(location.getWidth(), location.getHeight());
        stamp.setAppearance(PdfName.N, app);

        PdfArray stickyRect = stamp.getAsArray(PdfName.RECT);
        Rectangle annotRect = new Rectangle(stickyRect.getAsNumber(0).floatValue(),
            stickyRect.getAsNumber(1).floatValue(),
            stickyRect.getAsNumber(2).floatValue(),
            stickyRect.getAsNumber(3).floatValue());

        // Create cloud appearance
        CBorder cborder = new CBorder(app, 1, 1, annotRect);
        cborder.createCloudyRectangle(null);

        stamp.put(PdfName.RECT, new PdfRectangle(cborder.getRectangle()));
        stamp.put(PdfName.RD, new PdfArray(new float[] { 
            cborder.getRectDifference().getLeft(), 
            cborder.getRectDifference().getBottom(), 
            cborder.getRectDifference().getRight(), 
            cborder.getRectDifference().getTop() }));

        app.rectangle(cborder.getBBox());
        app.transform(cborder.getMatrix());


        app.setColorStroke(BaseColor.RED);
        app.setLineWidth(1);
        app.stroke();

        stamper.addAnnotation(stamp, 1);
        stamper.close();
        reader.close();
    }

    /**
     * <a href="https://stackoverflow.com/questions/56388137/box-cloud-annotation-appearance-in-itext">
     * Box Cloud Annotation Appearance in iText
     * </a>
     * <p>
     * This code fixes the issue in the OP's original code: It makes the
     * appearance stream valid by removing the arbitrary instructions
     * between path definition and path drawing, and it properly applies
     * the bounding box of the cloud to the appearance.
     * </p>
     * @see #testDrawLikeChitgoks()
     */
    @Test
    public void testDrawLikeChitgoksImproved() throws IOException, DocumentException {
        PdfReader reader = new PdfReader(getClass().getResourceAsStream("file.pdf"));
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, "CloudBoxLikeChitgoksImproved.pdf")));

        PdfDictionary be = new PdfDictionary();
        be.put(PdfName.S, PdfName.C);
        be.put(PdfName.I, new PdfNumber(1));

        Rectangle location = new Rectangle(123.6f, 584.4f, 252.6f, 653.4f);
        PdfAnnotation stamp = PdfAnnotation.createSquareCircle(stamper.getWriter(), location, "", true);
        stamp.setBorderStyle(new PdfBorderDictionary(1, PdfBorderDictionary.STYLE_SOLID));
        stamp.put(new PdfName("BE"), be);
        stamp.setColor(BaseColor.RED);

        PdfContentByte cb = stamper.getOverContent(1);
        PdfAppearance app = cb.createAppearance(location.getWidth(), location.getHeight());
        stamp.setAppearance(PdfName.N, app);

        PdfArray stickyRect = stamp.getAsArray(PdfName.RECT);
        Rectangle annotRect = new Rectangle(stickyRect.getAsNumber(0).floatValue(),
            stickyRect.getAsNumber(1).floatValue(),
            stickyRect.getAsNumber(2).floatValue(),
            stickyRect.getAsNumber(3).floatValue());

        // Create cloud appearance
        app.setColorStroke(BaseColor.RED);
        app.setLineWidth(1);

        CBorder cborder = new CBorder(app, 1, 1, annotRect);
        cborder.createCloudyRectangle(null);

        stamp.put(PdfName.RECT, new PdfRectangle(cborder.getRectangle()));
        stamp.put(PdfName.RD, new PdfArray(new float[] { 
            cborder.getRectDifference().getLeft(), 
            cborder.getRectDifference().getBottom(), 
            cborder.getRectDifference().getRight(), 
            cborder.getRectDifference().getTop() }));

        app.stroke();
        app.setBoundingBox(cborder.getBBox());

        stamper.addAnnotation(stamp, 1);
        stamper.close();
        reader.close();
    }
}
