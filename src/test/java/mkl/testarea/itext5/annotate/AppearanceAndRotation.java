package mkl.testarea.itext5.annotate;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfAppearance;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfBorderDictionary;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class AppearanceAndRotation {
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/48137530/freetext-annotation-appearance-stream-in-landscape-pdf-using-itext">
     * FreeText Annotation Appearance Stream In Landscape PDF Using iText
     * </a>
     * <p>
     * This test is a first approximation to what the OP may want; based
     * on this some clarifications were requested.
     * </p>
     */
    @Test
    public void testCreateWithAppearanceAndRotation() throws DocumentException, IOException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "appearanceAndRotation.pdf")));
        document.open();

        Rectangle rect = new Rectangle(50, 630, 150, 660);
        String contents = "Test";
        PdfAnnotation annotation = createAnnotation(writer, rect, contents);

        document.add(new Paragraph("Test paragraph"));
        writer.addAnnotation(annotation);

        document.setPageSize(PageSize.A4.rotate());
        document.newPage();

        rect = new Rectangle(50, 380, 80, 480);
        contents = "NoRotate flag";
        annotation = createAnnotation(writer, rect, contents);
        annotation.setFlags(PdfAnnotation.FLAGS_NOROTATE);

        document.add(new Paragraph("Test paragraph"));
        writer.addAnnotation(annotation);

        document.close();
    }

    /**
     * @see #testCreateWithAppearanceAndRotation()
     */
    PdfAnnotation createAnnotation(PdfWriter writer, Rectangle rect, String contents) throws DocumentException, IOException {
        PdfContentByte cb = writer.getDirectContent();
        PdfAppearance cs = cb.createAppearance(rect.getWidth(), rect.getHeight());

        cs.rectangle(0 , 0, rect.getWidth(), rect.getHeight());
        cs.fill();

        cs.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED), 12);                        
        cs.beginText();
        cs.setLeading(12 + 1.75f);
        cs.moveText(.75f, rect.getHeight() - 12 + .75f);
        cs.showText(contents);
        cs.endText();

        return PdfAnnotation.createFreeText(writer, rect, contents, cs);
    }

    /**
     * <a href="https://stackoverflow.com/questions/48200793/line-drawing-in-landscape-mode-using-itext">
     * Line Drawing In landscape mode using iText
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1HtA0kOF7oV2i5J2a_EuPN2jegLQEsvai">
     * Test.java
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1fXDVx486oMyZtXfuoHtDCdEBgAXWKvr7">
     * result.pdf
     * </a>
     * <p>
     * When this code was the current state of the question, the OP unfortunately
     * did not make clear what the problem is, in particular that he used Chrome
     * as viewer. Thus, the missing annotation did not become clear.
     * </p>
     * <p>
     * Meanwhile, after the OP provided a new version of his code, confer the test
     * {@link #testCreateLineWithAppearanceAndRotation2()}, and also said he used
     * Chrome, the missing appearance stream became apparent, and just like in that
     * other test it is caused by writing the annotation to the result file (in
     * <code>stamper.addAnnotation(stamp, 1)</code> before creating and adding an
     * appearance stream.
     * </p>
     */
    @Test
    public void testCreateLineWithAppearanceAndRotation() throws DocumentException, IOException {
        try (   InputStream resource = getClass().getResourceAsStream("result.pdf") ) {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, "result-with-line.pdf")));
            
            Rectangle location = new Rectangle(142.55415f, 520.3019f, 342.13f, 698.26416f);

            double[] xx = new double[2];
            double[] yy = new double[2];
            
            xx[0] = 520.3018798828125;
            yy[0] = 881.8699951171875;
            xx[1] = 698.26416015625;
            yy[1]= 1081.44580078125;        

            PdfArray lineEndings = new PdfArray();
            lineEndings.add(new PdfName("OpenArrow"));
            lineEndings.add(new PdfName("None"));

            PdfAnnotation stamp = PdfAnnotation.createLine(stamper.getWriter(), location, 
                "comment",  (float) xx[0], (float) yy[0], (float) xx[1], (float) yy[1]);
            stamp.put(new PdfName("LE"), lineEndings);
            stamp.put(new PdfName("IT"), new PdfName("LineArrow"));
            stamp.setBorderStyle(new PdfBorderDictionary(1, PdfBorderDictionary.STYLE_SOLID));
            stamp.setColor(PdfGraphics2D.prepareColor(Color.RED));
            stamper.addAnnotation(stamp, 1);
            
            // appearance stream
            PdfContentByte cb = stamper.getOverContent(1);
            PdfAppearance app = cb.createAppearance(location.getWidth(),  location.getHeight());        
            
            PdfArray rect = stamp.getAsArray(PdfName.RECT);
            Rectangle bbox = app.getBoundingBox();
            
            double[] lineArray = stamp.getAsArray(PdfName.L).asDoubleArray();
            double x1 = lineArray[0] - rect.getAsNumber(0).doubleValue();
            double y1 = lineArray[1] - rect.getAsNumber(1).doubleValue();
            double x2 = lineArray[2] - rect.getAsNumber(0).doubleValue();
            double y2 = lineArray[3] - rect.getAsNumber(1).doubleValue();
            
            app.moveTo(x1, y1);
            app.lineTo(x2, y2);

            double dx = x2 - x1;
            double dy = y2 - y1;
            double arrowAngle = Math.atan2(dy, dx); 
            int arrowLength  = 10;

             // Draw arrow head.
             app.moveTo(x1, y1);
             app.lineTo((float) (x2 - arrowLength * Math.cos(arrowAngle - Math.PI / 6)),
                 (float) (y2 - arrowLength * Math.sin(arrowAngle - Math.PI / 6)));
             app.moveTo(x2, y2);
             app.lineTo((float) (x2 - arrowLength * Math.cos(arrowAngle + Math.PI / 6)), 
                 (float) (y2 - arrowLength * Math.sin(arrowAngle + Math.PI / 6)));
         
            app.stroke();
            stamp.setAppearance(PdfName.N, app);
            
            stamper.close();
            reader.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/48200793/line-drawing-in-landscape-mode-using-itext">
     * Line Drawing In landscape mode using iText
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1HtA0kOF7oV2i5J2a_EuPN2jegLQEsvai">
     * Test.java
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1Opv5AyF8AkTD9fiphoxCbhviGlZwL2PM">
     * result.pdf
     * </a>
     * <p>
     * In contrast to what the OP wanted, the appearance stream is missing. This
     * is caused by writing the annotation to the result file (in
     * <code>stamper.addAnnotation(stamp, 1)</code> before creating and adding an
     * appearance stream (in <code>addAppearance(stamper, stamp, location)</code>).
     * </p>
     * <p>
     * This is fixed in {@link #testCreateLineWithAppearanceAndRotation2Improved()}.
     * </p>
     */
    @Test
    public void testCreateLineWithAppearanceAndRotation2() throws DocumentException, IOException {
        try (   InputStream resource = getClass().getResourceAsStream("result.pdf") ) {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, "result-with-line-2.pdf")));

            Rectangle location = new Rectangle(544.8f, 517.65f, 663f, 373.35f);

            PdfArray lineEndings = new PdfArray();
            lineEndings.add(new PdfName("None"));
            lineEndings.add(new PdfName("None"));

            PdfAnnotation stamp = PdfAnnotation.createLine(stamper.getWriter(), location, 
                "comment",  550.05f, 510.9f, 656.25f, 378.6f);
            stamp.put(new PdfName("LE"), lineEndings);
            stamp.put(new PdfName("IT"), new PdfName("Line"));
            stamp.setBorderStyle(new PdfBorderDictionary(1, PdfBorderDictionary.STYLE_SOLID));
            stamp.setColor(PdfGraphics2D.prepareColor(Color.RED));
            stamp.put(PdfName.ROTATE, new PdfNumber(270));

            stamper.addAnnotation(stamp, 1);

            addAppearance(stamper, stamp, location);

            stamper.close();
            reader.close();
        }
    }

    /**
     * @see #testCreateLineWithAppearanceAndRotation2()
     */
    private static void addAppearance(PdfStamper stamper, PdfAnnotation stamp, Rectangle location) {
        PdfContentByte cb = stamper.getOverContent(1);
        PdfAppearance app = cb.createAppearance(location.getWidth(),  location.getHeight());        
        
        PdfArray rect = stamp.getAsArray(PdfName.RECT);
        Rectangle bbox = app.getBoundingBox();
        
        double[] lineArray = stamp.getAsArray(PdfName.L).asDoubleArray();
        double x1 = lineArray[0] - rect.getAsNumber(0).doubleValue();
        double y1 = lineArray[1] - rect.getAsNumber(1).doubleValue();
        double x2 = lineArray[2] - rect.getAsNumber(0).doubleValue();
        double y2 = lineArray[3] - rect.getAsNumber(1).doubleValue();
        
        app.moveTo(x1, y1);
        app.lineTo(x2, y2);
     
        app.stroke();
        stamp.setAppearance(PdfName.N, app);

    }

    /**
     * <a href="https://stackoverflow.com/questions/48200793/line-drawing-in-landscape-mode-using-itext">
     * Line Drawing In landscape mode using iText
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1HtA0kOF7oV2i5J2a_EuPN2jegLQEsvai">
     * Test.java
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1Opv5AyF8AkTD9fiphoxCbhviGlZwL2PM">
     * result.pdf
     * </a>
     * <p>
     * This code fixes one issue in {@link #testCreateLineWithAppearanceAndRotation2()},
     * the wrong order of writing the annotation to file and creating and adding an
     * appearance.
     * </p>
     * <p>
     * Furthermore it prevents iText from trying to help while manipulating rotated
     * pages, a help which here damages. It does so by temporarily removing the page
     * rotation.
     * </p>
     */
    @Test
    public void testCreateLineWithAppearanceAndRotation2Improved() throws DocumentException, IOException {
        try (   InputStream resource = getClass().getResourceAsStream("result.pdf") ) {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, "result-with-line-2-improved.pdf")));

            // hide the page rotation
            PdfDictionary pageDict = reader.getPageN(1);
            PdfNumber rotation = pageDict.getAsNumber(PdfName.ROTATE);
            pageDict.remove(PdfName.ROTATE);

            Rectangle location = new Rectangle(544.8f, 517.65f, 663f, 373.35f);

            PdfArray lineEndings = new PdfArray();
            lineEndings.add(new PdfName("None"));
            lineEndings.add(new PdfName("None"));

            PdfAnnotation stamp = PdfAnnotation.createLine(stamper.getWriter(), location, 
                "comment",  550.05f, 510.9f, 656.25f, 378.6f);
            stamp.put(new PdfName("LE"), lineEndings);
            stamp.put(new PdfName("IT"), new PdfName("Line"));
            stamp.setBorderStyle(new PdfBorderDictionary(1, PdfBorderDictionary.STYLE_SOLID));
            stamp.setColor(PdfGraphics2D.prepareColor(Color.RED));
            stamp.put(PdfName.ROTATE, new PdfNumber(270));

            addAppearance(stamper, stamp, location);

            stamper.addAnnotation(stamp, 1);

            // add page rotation again if required
            if (rotation != null)
                pageDict.put(PdfName.ROTATE, rotation);

            stamper.close();
            reader.close();
        }
    }

}
