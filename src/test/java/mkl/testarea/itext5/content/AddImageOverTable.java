package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mklink
 *
 */
public class AddImageOverTable {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/53095527/add-image-above-table-itext">
     * Add image above table - Itext
     * </a>
     * <p>
     * Tests the OP's code. Indeed, the table lines always were on top
     * of the image, no matter which was added first. The cause was that
     * bitmap images via {@link Document#add(Element)} are added to a
     * virtual layer under text and table data. The solution was to add
     * the image to {@link PdfWriter#getDirectContent()} instead of to
     * the {@link Document}, cf. {@link TemplatePDF#addImg(int, float, float, float)}.
     * </p>
     */
    @Test
    public void testLikeWotonSampaio() throws DocumentException, IOException {
        //Creating the object
        TemplatePDF templatePDF = new TemplatePDF();
        templatePDF.openDocument();
        //templatePDF.addMetaData("Relatório", "Situs", "Woton Sampaio");
        //templatePDF.addTitles("Relatório", "","Data: " + getDate());

        //Creating the table
        ArrayList<String> header = new ArrayList<>();
        for(int i = 0; i < 55; i++){
            header.add(String.valueOf(i));
        }

        //ArrayList<pdfItens> itens = arrayItens();
        //ArrayList<String[]> files = array();
        ArrayList<String[]> files = new ArrayList<>();
        String[] file = new String[55];
        files.add(file);
        for(int i = 0; i < 55; i++){
            file[i] = "c" + i;
        }
        

        //templatePDF.createHeaderFicha(itens);
        templatePDF.createTable(header, files);

        //Adding image
        templatePDF.addImg(0, 0, 20, 566 + 260);
        
        templatePDF.closeDocument();
    }

    /**
     * @see AddImageOverTable#testLikeWotonSampaio()
     */
    public class TemplatePDF {
        private File pdfFile;
        private Document document;
        public PdfWriter pdfWriter;
        private Paragraph paragraph;
        private Font fTitle = new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.BOLD);
        private Font fSubTitle = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
        private Font fHeaderText = new Font(Font.FontFamily.TIMES_ROMAN, 3, Font.NORMAL, BaseColor.WHITE);
        private Font fText = new Font(Font.FontFamily.TIMES_ROMAN, 3);
        private Font fHText = new Font(Font.FontFamily.TIMES_ROMAN, 8);
        private Font fHighText = new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.BOLD, BaseColor.RED);
        private float width = PageSize.A4.getWidth();
        private float height = PageSize.A4.getHeight();
        public float sizeImg;
        public float sizeImgFit;

        public void openDocument() throws IOException, DocumentException{
            createFile();

            document = new Document(PageSize.A4);
            pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();
        }

        private void createFile(){
            pdfFile = new File(RESULT_FOLDER, "AddImageOverTable.pdf");
        }

        public void closeDocument(){
            document.close();
        }

        public void addImg (int dwb, float x, float y, float desc) throws MalformedURLException, IOException, DocumentException {
//            Bitmap bitmap = dwbToBitmap(context, dwb);
//            ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream3);

            Image image = Image.getInstance("src\\test\\resources\\mkl\\testarea\\itext5\\content\\2x2colored.png");
//            stream3.close();
            image.scaleToFit(sizeImgFit, sizeImgFit);
            image.setAbsolutePosition(35.6f + 10f + x, height-y-sizeImg-(height-desc));
            //image.setLayer(layer_3);

//The FIX: add image to DirectContent instead of Document
            //document.add(image);
            pdfWriter.getDirectContent().addImage(image);
        }

        public void createTable(ArrayList<String> header, ArrayList<String[]> clients) throws DocumentException{
            float height = 569/header.size();
            sizeImg = height;
            sizeImgFit = sizeImg - 2;
            PdfPTable pdfPTable = new PdfPTable(header.size());
            pdfPTable.setWidthPercentage(100);

            PdfPCell pdfPCell;

            int indexC = 0;

            while(indexC < header.size()){
                pdfPCell = new PdfPCell(new Phrase(header.get(indexC++), fHeaderText));
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(BaseColor.GRAY);
                pdfPTable.addCell(pdfPCell);
            }

            for(String[] row : clients){
                for(String linha : row){
                    pdfPCell = new PdfPCell(new Phrase(linha, fText));
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setFixedHeight(height);

                    pdfPTable.addCell(pdfPCell);
                }
            }

            //paragraph.add(pdfPTable);

            document.add(pdfPTable);
        }
    }
}
