package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class AddSpecialCharacters {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/55947168/how-to-display-multilingual-or-any-unicode-special-character-in-itextpdf-pdf-whi">
     * How to display multilingual or any UNICODE special character in itextpdf PDF which is accepted from UI?
     * </a>
     * <p>
     * Cannot reproduce the issue with current c:\Windows\Fonts\arial.ttf.
     * </p>
     */
    @Test
    public void testAddLikeKiranBadave() throws IOException, DocumentException
    {
        try (   FileOutputStream stream = new FileOutputStream(new File(RESULT_FOLDER, "specialCharactersLikeKiranBadave.pdf"))    )
        {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, stream);
            document.open();

            BaseFont bf = BaseFont.createFont("c:\\Windows\\Fonts\\arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            Paragraph p = new Paragraph("Şinasi ıssız ile ağaç", new Font(bf, 22));

            document.add(p);

            document.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/68206055/helvetica-from-itext-doesnt-show-all-helvetica-characters">
     * Helvetica from itext doesn't show all helvetica characters?
     * </a>
     * <p>
     * Cannot reproduce the issue.
     * </p>
     */
    @Test
    public void testAddLikeLouis33() throws IOException, DocumentException {
        try (   FileOutputStream stream = new FileOutputStream(new File(RESULT_FOLDER, "specialCharactersLikeLouis33.pdf")) )
        {
            Document document = new Document();
            PdfWriter.getInstance(document, stream);
            document.open();

            PdfPTable table = new PdfPTable(1);
            table.addCell(createCellLikeLouis33("Apostrophes: `'´"));
            table.addCell(createCellLikeLouis33("Ellipsis: …"));
            document.add(table);

            document.close();
        }
    }

    PdfPCell createCellLikeLouis33(String val) {
        Font font = new Font(FontFamily.HELVETICA);
        font.setSize(12);
        PdfPCell cell = new PdfPCell(new Paragraph(val, font));
        cell.setMinimumHeight(20);
        cell.setIndent(30);
        cell.setFollowingIndent(30);
        return cell;
    }
}
