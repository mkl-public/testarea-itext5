package mkl.testarea.itext5.annotate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class CreateLink
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/34408764/create-local-link-in-rotated-pdfpcell-in-itextsharp">
     * Create local link in rotated PdfPCell in iTextSharp
     * </a>
     * <p>
     * This is the equivalent Java code for the C# code in the question. Indeed, this code
     * also gives rise to the broken result. The cause is simple: Normally iText does not
     * touch the current transformation matrix. So the chunk link creation code assumes the
     * current user coordinate system to be the same as used for positioning annotations.
     * But in case of rotated cells iText does change the transformation matrix and
     * consequently the chunk link creation code positions the annotation at the wrong
     * location.
     * </p>
     */
    @Test
    public void testCreateLocalLinkInRotatedCell() throws IOException, DocumentException
    {
        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(new File(RESULT_FOLDER, "local-link.pdf")));
        doc.open();

        PdfPTable linkTable = new PdfPTable(2);
        PdfPCell linkCell = new PdfPCell();

        linkCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        linkCell.setRotation(90);
        linkCell.setFixedHeight(70);

        Anchor linkAnchor = new Anchor("Click here");
        linkAnchor.setReference("#target");
        Paragraph linkPara = new Paragraph();
        linkPara.add(linkAnchor);
        linkCell.addElement(linkPara);
        linkTable.addCell(linkCell);

        PdfPCell linkCell2 = new PdfPCell();
        Anchor linkAnchor2 = new Anchor("Click here 2");
        linkAnchor2.setReference("#target");
        Paragraph linkPara2 = new Paragraph();
        linkPara2.add(linkAnchor2);
        linkCell2.addElement(linkPara2);
        linkTable.addCell(linkCell2);

        linkTable.addCell(new PdfPCell(new Phrase("cell 3")));
        linkTable.addCell(new PdfPCell(new Phrase("cell 4")));
        doc.add(linkTable);

        doc.newPage();

        Anchor destAnchor = new Anchor("top");
        destAnchor.setName("target");
        PdfPTable destTable = new PdfPTable(1);
        PdfPCell destCell = new PdfPCell();
        Paragraph destPara = new Paragraph();
        destPara.add(destAnchor);
        destCell.addElement(destPara);
        destTable.addCell(destCell);
        destTable.addCell(new PdfPCell(new Phrase("cell 2")));
        destTable.addCell(new PdfPCell(new Phrase("cell 3")));
        destTable.addCell(new PdfPCell(new Phrase("cell 4")));
        doc.add(destTable);

        doc.close();
    }

    /**
     * <a href="http://stackoverflow.com/questions/34734669/define-background-color-and-transparency-of-link-annotation-in-pdf">
     * Define background color and transparency of link annotation in PDF
     * </a>
     * <p>
     * This test creates a link annotation with custom appearance. Adobe Reader chooses
     * to ignore it but other viewers use it. Interestingly Adobe Acrobat export-as-image
     * does use the custom appearance...
     * </p>
     */
    @Test
    public void testCreateLinkWithAppearance() throws IOException, DocumentException
    {
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(new File(RESULT_FOLDER, "custom-link.appearance.pdf")));
        writer.setCompressionLevel(0);
        doc.open();

        BaseFont baseFont = BaseFont.createFont();
        int fontSize = 15;
        doc.add(new Paragraph("Hello", new Font(baseFont, fontSize)));
        
        PdfContentByte content = writer.getDirectContent();
        
        String text = "Test";
        content.setFontAndSize(baseFont, fontSize);
        content.beginText();
        content.moveText(100, 500);
        content.showText(text);
        content.endText();
        
        Rectangle linkLocation = new Rectangle(95, 495 + baseFont.getDescentPoint(text, fontSize),
                105 + baseFont.getWidthPoint(text, fontSize), 505 + baseFont.getAscentPoint(text, fontSize));

        PdfAnnotation linkGreen = PdfAnnotation.createLink(writer, linkLocation, PdfName.HIGHLIGHT, "green" );
        PdfTemplate appearance = PdfTemplate.createTemplate(writer, linkLocation.getWidth(), linkLocation.getHeight());
        PdfGState state = new PdfGState();
        //state.FillOpacity = .3f;
        // IMPROVEMENT: Use blend mode Darken instead of transparency; you may also want to try Multiply.
        state.setBlendMode(new PdfName("Darken"));
        appearance.setGState(state);

        appearance.setColorFill(BaseColor.GREEN);
        appearance.rectangle(0, 0, linkLocation.getWidth(), linkLocation.getHeight());
        appearance.fill();
        linkGreen.setAppearance(PdfName.N, appearance);
        writer.addAnnotation(linkGreen);

        doc.open();
        doc.close();
    }

    /**
     * <a href="https://stackoverflow.com/questions/51364373/add-hyperlink-inside-a-cell-event-in-itext">
     * Add hyperlink inside a cell event in itext
     * </a>
     * <p>
     * This test shows that the {@link AddHyperLink} cell event listener of
     * the OP does work if given a large enough cell to work in.
     * </p>
     */
    @Test
    public void testCreateLinkInCellEvent() throws IOException, DocumentException {
        try (InputStream imageStream = getClass().getResourceAsStream("/mkl/testarea/itext5/layer/Willi-1.jpg "))
        {
            Image image = Image.getInstance(IOUtils.toByteArray(imageStream));
            image.scaleToFit(110,110);

            Document doc = new Document();
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(new File(RESULT_FOLDER, "link-in-cell-event.pdf")));
            writer.setCompressionLevel(0);
            doc.open();

            PdfPTable table = new PdfPTable(1);
            PdfPCell cell = new PdfPCell(image);
            cell.setCellEvent(new AddHyperLink());

            table.addCell(cell);
            doc.add(table);

            doc.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/51364373/add-hyperlink-inside-a-cell-event-in-itext">
     * Add hyperlink inside a cell event in itext
     * </a>
     * <p>
     * The original code of the OP with the obvious correction.
     * The test {@link CreateLink#testCreateLinkInCellEvent()} shows that it works.
     * </p>
     */
    private static class AddHyperLink implements PdfPCellEvent {

        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {

            Paragraph mainPragraph = new Paragraph();
            Chunk descCk = new Chunk("This is ");

            mainPragraph.add(descCk);
            Chunk orgDiscriptionMore = new Chunk("HyperLink");
            orgDiscriptionMore.setAnchor("http://www.google.com");
            mainPragraph.add(orgDiscriptionMore);

            PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
            ColumnText ct = new ColumnText(canvas);

            ct.setSimpleColumn(position);
            ct.addElement(mainPragraph);

            try {
                ct.go();
            } catch (DocumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/53264518/unicode-symbols-in-itextsharp-relative-link">
     * Unicode symbols in iTextSharp relative link
     * </a>
     * <p>
     * This test checks how to create a link to a local file with
     * non-ASCII characters in its path. Unfortunately iText 5.x
     * ignores the possibility that special characters might be
     * contained in the anchor argument, applies the standard PDF
     * document encryption, and actually drops all characters not
     * in that encoding.
     * </p>
     * <p>
     * The two options working properly are either injecting the UTF-8
     * encoded URI ("Cyrillic chars in target. Action manipulated.")
     * or URL encoding the URI beforehand ("Cyrillic chars in target.
     * URL-encoded."). Actually the former method makes use of a PDF-2
     * specific feature, UTF-8 encoded strings, so the latter one may
     * be preferable.
     * </p>
     * <p>
     * Probably the Launch Action variant can be made working, too, by
     * finding the proper encoding to inject the file path with.
     * </p>
     */
    @Test
    public void testCreateLinkWithSpecialCharactersTarget() throws IOException, DocumentException {
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(new File(RESULT_FOLDER, "link-special-char-target.pdf")));
        writer.setCompressionLevel(0);
        doc.open();

        Chunk chunk = new Chunk("Only ASCII chars in target.");
        chunk.setAnchor("./Attachments/1.jpg");
        doc.add(new Paragraph(chunk));

        chunk = new Chunk("Cyrillic chars in target.");
        chunk.setAnchor("./Вложения/1.jpg");
        doc.add(new Paragraph(chunk));

        chunk = new Chunk("Cyrillic chars in launch.");
        chunk.setAction(new PdfAction("./Вложения/1.jpg", null, null, null));
        doc.add(new Paragraph(chunk));

        chunk = new Chunk("Cyrillic chars in target. Action manipulated.");
        chunk.setAnchor("./Вложения/1.jpg");
        PdfAction action = (PdfAction) chunk.getAttributes().get(Chunk.ACTION);
        action.put(PdfName.URI, new PdfString("./Вложения/1.jpg".getBytes("UTF8")));
        doc.add(new Paragraph(chunk));

        chunk = new Chunk("Cyrillic chars in launch. Action manipulated.");
        chunk.setAction(new PdfAction("./Вложения/1.jpg", null, null, null));
        action = (PdfAction) chunk.getAttributes().get(Chunk.ACTION);
        action.put(PdfName.F, new PdfString("./Вложения/1.jpg".getBytes("UTF8")));
        doc.add(new Paragraph(chunk));

        chunk = new Chunk("Cyrillic chars in target. URL-encoded.");
        chunk.setAnchor(URLEncoder.encode("./Вложения/1.jpg", "UTF8"));
        doc.add(new Paragraph(chunk));

        doc.close();
    }
}
