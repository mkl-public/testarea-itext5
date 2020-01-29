package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class CreateTableDirectContent
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/43807931/creating-table-in-pdf-on-last-page-bottom-wrong-official-solution">
     * Creating table in pdf on last page bottom (wrong official solution)
     * </a>
     * <p>
     * Indeed, there is an error in the official sample which effectively
     * applies the margins twice.
     * </p>
     */
    @Test
    public void testCreateTableLikeUser7968180() throws FileNotFoundException, DocumentException
    {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(new File(RESULT_FOLDER, "calendarUser7968180.pdf")));
        document.open();

        PdfPTable datatable = null;//createHeaderTable();
        //document.add(datatable);
        datatable = createFooterTable();

        drawTableAtTheEndOfPage(document, writer, datatable);

        // Marking the border
        PdfContentByte canvas = writer.getDirectContentUnder();
        canvas.setColorStroke(BaseColor.RED);
        canvas.setColorFill(BaseColor.PINK);
        canvas.rectangle(document.left(), document.bottom(), document.right() - document.left(), document.top() - document.bottom());
        Rectangle pageSize = document.getPageSize(); 
        canvas.rectangle(pageSize.getLeft(), pageSize.getBottom(), pageSize.getWidth(), pageSize.getHeight());
        canvas.eoFillStroke();

        document.close();
        System.out.println("done");
    }

    /**
     * <a href="http://stackoverflow.com/questions/43807931/creating-table-in-pdf-on-last-page-bottom-wrong-official-solution">
     * Creating table in pdf on last page bottom (wrong official solution)
     * </a>
     * <p>
     * Helper method for {@link #testCreateTableLikeUser7968180()}. Here the error
     * is corrected.
     * </p>
     */
    private static void drawTableAtTheEndOfPage(Document document, PdfWriter writer, PdfPTable datatable)
    {
        datatable.setTotalWidth(document.right() - document.left());
//        datatable.setTotalWidth(document.right(document.rightMargin()) - document.left(document.leftMargin()));

        datatable.writeSelectedRows(0, -1, document.left(),
                datatable.getTotalHeight() + document.bottom(), writer.getDirectContent());
//        datatable.writeSelectedRows(0, -1, document.left(document.leftMargin()),
//                datatable.getTotalHeight() + document.bottom(document.bottomMargin()), writer.getDirectContent());
    }

    /**
     * <a href="http://stackoverflow.com/questions/43807931/creating-table-in-pdf-on-last-page-bottom-wrong-official-solution">
     * Creating table in pdf on last page bottom (wrong official solution)
     * </a>
     * <p>
     * Helper method for {@link #testCreateTableLikeUser7968180()}.
     * </p>
     */
    private static PdfPTable createFooterTable() throws DocumentException
    {
        int[] columnWidths = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 };
        PdfPTable datatable = new PdfPTable(columnWidths.length);
        datatable.setKeepTogether(true);
        datatable.setWidthPercentage(100);
        datatable.setWidths(columnWidths);
        datatable.getDefaultCell().setPadding(5);

//        datatable.getDefaultCell().setHorizontalAlignment(horizontalAlignment);
//        datatable.getDefaultCell().setVerticalAlignment(verticalAlignment);

        for (int i = 0; i < 100; i++)
        {
            datatable.addCell("Přehledová tabulka");
//            addCellToTable(datatable, horizontalAlignmentLeft, verticalAlignmentMiddle, "Přehledová tabulka",
//                    columnWidths.length, 1, fontTypeBold, fontSizeRegular, cellLayout_Bottom);
        }

        return datatable;
    }

    /**
     * <a href="https://stackoverflow.com/questions/59944058/itext-5-table-rows-splitting-to-new-page-and-repeating">
     * Itext 5 Table rows splitting to new page and repeating
     * </a>
     * <p>
     * The repetitions are due to only advancing by half the section width
     * after painting a section causing each following section to start with
     * the second half of the previous one.
     * </p>
     * <p>
     * Fixed by advancing twice as far, i.e. the whole section width.
     * </p>
     */
    @Test
    public void testCreateTableLikeMarothiLetsoalo() throws IOException, DocumentException {
//        ProductCondition prodCond = new ProductCondition(true,"GREEN","BFobrourinbiurfufbjfnbbu");
//        ProductOperations prodOper = new ProductOperations(true,true,true,1000,986,500,"OBNobdfiuvdob");
//        Product product = new Product("bkbukb","sfdvsf","sdfsdfs","1sfdssV45",prodCond,prodOper);
//        technicians.addProduct(product);
        Document document = new Document(PageSize.A1.rotate());
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "TableLikeMarothiLetsoalo.pdf")));
        document.open();
        PdfPTable table = new PdfPTable(15);
        table.setTotalWidth(5000);
        table.setWidthPercentage(100);
        List<String> listData =new ArrayList<>() ;
//        listData = GetTechnicianData(technicians);
        for (int c = 0; c < 14; c++)
            listData.add(String.valueOf(c));
        Image image1 = GetImage();
        Image image2 = GetImage();
        table.addCell("Bloop");
        table.addCell("han Solo");
        table.addCell("Hamburger");
        table.addCell("NUmber time");
        table.addCell("boogaloo");
        table.addCell("Boo thang");
        table.addCell("Spanish");
        table.addCell("Inquisition");
        table.addCell("Never ");
        table.addCell("Death");
        table.addCell("Test ");
       // table.addCell("Button");
        table.addCell("Lights");
        table.addCell("Sunshine");
        table.addCell("Comment");
        table.addCell("Images");
        table.setHeaderRows(3);
        table.setFooterRows(1);
        table.getDefaultCell().setBackgroundColor(GrayColor.GRAYWHITE);
        PdfPCell cell;
//        Toast.makeText(this,"ADDING PRELIMINARY DATA",Toast.LENGTH_LONG).show();
            for (int c = 0; c < 14; c++) {
                cell = new PdfPCell();
                cell.setFixedHeight(50);
                cell.addElement(new Paragraph(listData.get(c)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                //cell.setBorder(PdfPCell.NO_BORDER);
                table.addCell(cell);
                table.setKeepTogether(true);
            }
        Paragraph p = new Paragraph();
        p.add(new Chunk(image1,0,0,true));
        p.add(new Chunk(image2,0,0,true));
        cell = new PdfPCell();
        cell.addElement(p);
        //cell.setBorder(PdfPCell.NO_BORDER);
        table.addCell(cell);
        //document.add(table);
//        Toast.makeText(this,"ADDING IMAGES...",Toast.LENGTH_LONG).show();

          /*  cell = new PdfPCell();
            Paragraph p = new Paragraph();
            p.add(new Chunk(image1,0,0,true));
            p.add(new Chunk(image1,0,0,true));
            cell.addElement(p);
            table.addCell(cell);*/

       // document.add(table);


        PdfContentByte canvas = writer.getDirectContent();
        PdfTemplate tableTemplate = canvas.createTemplate(5000, 2600);
        table.writeSelectedRows(0, -1, 0, 800, tableTemplate);
        PdfTemplate clip;
        for (int j = 0; j <5000; j += 2000) {  // originally j += 1000, i.e. originally only half the 2000 clip size
            table.setKeepTogether(true);
            document.newPage();
            for (int i = 2600; i > 0; i -= 1300) {

                clip = canvas.createTemplate(2000, 1300);
                clip.addTemplate(tableTemplate, -j, 1750 - i);
                canvas.addTemplate(clip, 50, 312);
                table.setKeepTogether(true);
                //canvas.addImage(image1);
            }
        }
        // byte [] pdf = Files.readAllBytes(file.toPath());
//        Uri filepdf = Uri.fromFile(new File(directory_path+filename));
//        UploadTask uploadTask = storageReference.child(technicians.getEmailAddress()).child("PDFUpdate").putFile(filepdf);
//        Toast.makeText(this,"PDF Generated Successfully",Toast.LENGTH_LONG).show();
        document.close();

       /* PackageManager packageManager = context.getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/pdf");
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "Download a PDF Viewer to see the generated PDF", Toast.LENGTH_SHORT).show();
        }
*/
    }

    private Image GetImage() throws BadElementException, IOException {
        Image image = Image.getInstance("src\\test\\resources\\mkl\\testarea\\itext5\\content\\2x2colored.png");
        image.scaleToFit(10, 10);
        return image;
    }
}
