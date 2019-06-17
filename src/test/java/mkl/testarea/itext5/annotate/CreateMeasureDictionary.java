package mkl.testarea.itext5.annotate;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfBoolean;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;

/**
 * @author mkl
 */
public class CreateMeasureDictionary {
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/56487403/creating-measure-dictionary-for-distance-annotation">
     * Creating measure dictionary for distance annotation
     * </a>
     * <p>
     * The displayed distance indeed is not the desired 14.5cm but
     * 10.87cm. So they in particular also aren't displayed in inches
     * either as claimed by the OP...
     * </p>
     * @see #testCreateLikeChitgoksImproved()
     * @see #createMeasureDictionary()
     */
    @Test
    public void testCreateLikeChitgoks() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf")) {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, "measureLikeChitgoks.pdf")));

            Rectangle location = new Rectangle(55.78125f, 493.875f, 253.59375f, 562.3125f);

            PdfDictionary dict = new PdfDictionary();

            PdfArray lineEndings = new PdfArray();
            lineEndings.add(new PdfName("OpenArrow"));
            lineEndings.add(new PdfName("OpenArrow"));

            PdfAnnotation stamp = PdfAnnotation.createLine(stamper.getWriter(), location, "test measurement", 55.78125f, 562.3125f, 253.59375f, 493.875f);
            stamp.put(new PdfName("LE"), lineEndings);
            stamp.put(PdfName.ROTATE, new PdfNumber(0));
            stamp.put(PdfName.MEASURE, createMeasureDictionary());
            stamp.put(new PdfName("IT"), new PdfName("LineDimension"));
            stamp.put(new PdfName("Cap"), new PdfBoolean(true));
            stamp.put(PdfName.F, new PdfNumber(516));

            stamp.setColor(PdfGraphics2D.prepareColor(Color.RED));
            stamper.addAnnotation(stamp, 1);
            stamper.close();
            reader.close();
        }
    }

    /** @see #testCreateLikeChitgoks() */
    private static PdfDictionary createMeasureDictionary() {
        PdfDictionary measureDictionary = new PdfDictionary(PdfName.MEASURE);
        measureDictionary.put(PdfName.R, new PdfString("1 cm = 1 cm"));

        PdfDictionary xDictionary = new PdfDictionary(PdfName.NUMBERFORMAT);

        xDictionary.put(PdfName.U, new PdfString("cm"));
        xDictionary.put(PdfName.C, new PdfNumber(0.0519548f));
        measureDictionary.put(PdfName.X, new PdfArray(xDictionary));

        PdfDictionary dDictionary = new PdfDictionary(PdfName.NUMBERFORMAT);
        dDictionary.put(PdfName.U, new PdfString("cm"));
        dDictionary.put(PdfName.C, new PdfNumber(1.0f));
        measureDictionary.put(PdfName.D, new PdfArray(dDictionary));

        PdfDictionary aDictionary = new PdfDictionary(PdfName.NUMBERFORMAT);
        aDictionary.put(PdfName.U, new PdfString("cm"));
        aDictionary.put(PdfName.C, new PdfNumber(1.0f));
        measureDictionary.put(PdfName.A, new PdfArray(aDictionary));

        return measureDictionary;
    }

    /**
     * <a href="https://stackoverflow.com/questions/56487403/creating-measure-dictionary-for-distance-annotation">
     * Creating measure dictionary for distance annotation
     * </a>
     * <p>
     * After correcting the conversion factor (@see {@link #createMeasureDictionaryImproved()}),
     * the displayed distance has become the desired 14.5cm.
     * </p>
     * @see #testCreateLikeChitgoks()
     * @see #createMeasureDictionaryImproved()
     */
    @Test
    public void testCreateLikeChitgoksImproved() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf")) {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, "measureLikeChitgoksImproved.pdf")));

            Rectangle location = new Rectangle(55.78125f, 493.875f, 253.59375f, 562.3125f);

            PdfDictionary dict = new PdfDictionary();

            PdfArray lineEndings = new PdfArray();
            lineEndings.add(new PdfName("OpenArrow"));
            lineEndings.add(new PdfName("OpenArrow"));

            PdfAnnotation stamp = PdfAnnotation.createLine(stamper.getWriter(), location, "test measurement", 55.78125f, 562.3125f, 253.59375f, 493.875f);
            stamp.put(new PdfName("LE"), lineEndings);
            stamp.put(PdfName.ROTATE, new PdfNumber(0));
            stamp.put(PdfName.MEASURE, createMeasureDictionaryImproved());
            stamp.put(new PdfName("IT"), new PdfName("LineDimension"));
            stamp.put(new PdfName("Cap"), new PdfBoolean(true));
            stamp.put(new PdfName("CP"), new PdfName("Top"));
            stamp.put(PdfName.F, new PdfNumber(516));

            stamp.setColor(PdfGraphics2D.prepareColor(Color.RED));
            stamper.addAnnotation(stamp, 1);
            stamper.close();
            reader.close();
        }
    }

    /** @see #testCreateLikeChitgoksImproved() */
    private static PdfDictionary createMeasureDictionaryImproved() {
        PdfDictionary measureDictionary = new PdfDictionary(PdfName.MEASURE);
        measureDictionary.put(PdfName.R, new PdfString("1 cm = 1 cm"));

        PdfDictionary xDictionary = new PdfDictionary(PdfName.NUMBERFORMAT);

        xDictionary.put(PdfName.U, new PdfString("cm"));
        xDictionary.put(PdfName.C, new PdfNumber(0.069273f));
        measureDictionary.put(PdfName.X, new PdfArray(xDictionary));

        PdfDictionary dDictionary = new PdfDictionary(PdfName.NUMBERFORMAT);
        dDictionary.put(PdfName.U, new PdfString("cm"));
        dDictionary.put(PdfName.C, new PdfNumber(1.0f));
        measureDictionary.put(PdfName.D, new PdfArray(dDictionary));

        PdfDictionary aDictionary = new PdfDictionary(PdfName.NUMBERFORMAT);
        aDictionary.put(PdfName.U, new PdfString("cm"));
        aDictionary.put(PdfName.C, new PdfNumber(1.0f));
        measureDictionary.put(PdfName.A, new PdfArray(aDictionary));

        return measureDictionary;
    }
}
