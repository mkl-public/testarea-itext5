// $Id$
package mkl.testarea.itext5.form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RadioCheckField;
import com.itextpdf.text.pdf.TextField;

/**
 * @author mkl
 */
public class AddField
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/35630320/itext-java-android-adding-fields-to-existing-pdf">
     * iText - Java Android - Adding fields to existing pdf
     * </a>
     * <p>
     * There actually are two issues in the OP's code:
     * </p>
     * <ol>
     * <li>he creates the fields with empty names
     * <li>he doesn't set border colors for his fields
     * </ol>
     * <p>
     * These issues are fixed in {@link #testAddLikePasquierCorentin()},
     * {@link CheckboxCellEvent}, and {@link MyCellField} below.
     * </p>
     */
    @Test
    public void testAddLikePasquierCorentin() throws IOException, DocumentException
    {
        File myFile = new File(RESULT_FOLDER, "preface-withField.pdf");
        
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/preface.pdf");
                OutputStream output = new FileOutputStream(myFile)  )
        {
            PdfReader pdfReader = new PdfReader(resource);

            pdfStamper = new PdfStamper(pdfReader, output);

            PdfContentByte canvas1;
            PdfContentByte canvas2;

            canvas1 = pdfStamper.getOverContent(1);
            canvas2 = pdfStamper.getOverContent(2);

            PdfPCell cellFillFieldPage1 = new PdfPCell();
// change: Use a non-empty name
            cellFillFieldPage1.setCellEvent(new MyCellField("A", 1));
            cellFillFieldPage1.setFixedHeight(15);
            cellFillFieldPage1.setBorder(Rectangle.NO_BORDER);
            cellFillFieldPage1.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cellCheckBoxPage2 = new PdfPCell();
// change: Use a non-empty name different from the one above
            cellCheckBoxPage2.setCellEvent(new CheckboxCellEvent("B", false, 2));
            cellCheckBoxPage2.setBorder(Rectangle.NO_BORDER);

            // ************** PAGE 1 ************** //

            // SET TABLE
            PdfPTable tableSection1Page1 = new PdfPTable(1);
            tableSection1Page1.setTotalWidth(136);
            tableSection1Page1.setWidthPercentage(100.0f);
            tableSection1Page1.setLockedWidth(true);

            // ADD CELLS TO TABLE
            tableSection1Page1.addCell(cellFillFieldPage1);


            // PRINT TABLES
            tableSection1Page1.writeSelectedRows(0, -1, 165, 530, canvas1);


            // ************ PAGE 2 ************ //

            // SET TABLES
            PdfPTable tableSection1Page2 = new PdfPTable(1);
            tableSection1Page2.setTotalWidth(10);
            tableSection1Page2.setWidthPercentage(100.0f);
            tableSection1Page2.setLockedWidth(true);

            // ADD CELLS TO TABLE
            tableSection1Page2.addCell(cellCheckBoxPage2);

            // PRINT TABLES
            tableSection1Page2.writeSelectedRows(0, -1, 182, 536, canvas2);

            // I tried this, but it didn't change anything
            pdfStamper.setFormFlattening(false);

            pdfStamper.close();
            pdfReader.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/35630320/itext-java-android-adding-fields-to-existing-pdf">
     * iText - Java Android - Adding fields to existing pdf
     * </a>
     * @see AddField#testAddLikePasquierCorentin()
     */
    PdfStamper pdfStamper;
    
    /**
     * <a href="http://stackoverflow.com/questions/35630320/itext-java-android-adding-fields-to-existing-pdf">
     * iText - Java Android - Adding fields to existing pdf
     * </a>
     * @see AddField#testAddLikePasquierCorentin()
     */
    class MyCellField implements PdfPCellEvent {
        protected String fieldname;
        protected int page;
        public MyCellField(String fieldname, int page) {
            this.fieldname = fieldname;
            this.page = page;
        }
        public void cellLayout(PdfPCell cell, Rectangle rectangle, PdfContentByte[] canvases) {
            final PdfWriter writer = canvases[0].getPdfWriter();
            final TextField textField = new TextField(writer, rectangle, fieldname);
// change: set border color
            textField.setBorderColor(BaseColor.BLACK);
            try {
                final PdfFormField field = textField.getTextField();
                pdfStamper.addAnnotation(field, page);
            } catch (final IOException ioe) {
                throw new ExceptionConverter(ioe);
            } catch (final DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/35630320/itext-java-android-adding-fields-to-existing-pdf">
     * iText - Java Android - Adding fields to existing pdf
     * </a>
     * @see AddField#testAddLikePasquierCorentin()
     */
    class CheckboxCellEvent implements PdfPCellEvent {
        protected String name;
        protected boolean check;
        protected int page;

        public CheckboxCellEvent(String name, boolean check, int page) {
            this.check = check;
            this.name = name;
            this.page = page;
        }

        public void cellLayout(PdfPCell cell, Rectangle position,
                               PdfContentByte[] canvases) {
            PdfWriter writer = canvases[0].getPdfWriter();
            float x = position.getLeft();
            float y = position.getBottom();
            Rectangle rect = new Rectangle(x-5, y-5, x+5, y+5);
            RadioCheckField checkbox = new RadioCheckField(
                    writer, rect, name, "Yes");
            checkbox.setCheckType(RadioCheckField.TYPE_CROSS);
            checkbox.setChecked(check);
// change: set border color
            checkbox.setBorderColor(BaseColor.BLACK);

            try {
                pdfStamper.addAnnotation(checkbox.getCheckField(), page);
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }
    }
}
