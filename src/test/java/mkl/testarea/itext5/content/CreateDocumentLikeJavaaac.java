package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * <a href="https://stackoverflow.com/questions/52131139/pdfwriter-document-has-no-content">
 * Pdfwriter: document has no content
 * </a>
 * <p>
 * This tests the inner class {@link Pdfgenerator} which essentially is the OP's
 * code with constants inserted for unknown expression values.
 * </p>
 * <p>
 * As it turns out, the code in its main path automatically must throw an
 * {@link IndexOutOfBoundsException}, see the first lines of the loop in
 * {@link Pdfgenerator#createTable(Document, Paragraph, String, String, int, int, int)}.
 * </p>
 * @author mkl
 */
public class CreateDocumentLikeJavaaac {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void test() throws IOException, DocumentException {
        new Pdfgenerator("debiteur", "bedrijf", 10, 5, 3);
    }

    /**
     * <a href="https://pastebin.com/zNJFHuyt">The OP's class</a> which essentially
     * is the OP's code with constants inserted for unknown expression values.
     * @author javaaac
     */
    public class Pdfgenerator {
        Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
        String datenow;
       
        public Pdfgenerator(String debiteur, String bedrijf, int termijn, int counts, int companynumber) throws IOException, DocumentException {
                Document document = new Document();
                String Filepath;
               
                if(companynumber==0)
                    Filepath = /*"./opslagaanmaningen/pyxis/"*/RESULT_FOLDER + "/" + debiteur + ".pdf";
                else
                    Filepath = /*"./opslagaanmaningen/zwaluw/"*/RESULT_FOLDER + "/" + debiteur + ".pdf";
               
                PdfWriter Writer = PdfWriter.getInstance(document, new FileOutputStream(Filepath));
                document.open();
                addMetaData(document);
                addTitlePage(document, debiteur, bedrijf, termijn, counts, companynumber);
                addline(Writer, counts);
                document.close();
               
        }
       
        private void addMetaData(Document document) {
            document.addTitle("Aanmaning");
            document.addSubject("Aanmaning");
            document.addKeywords("#####");
            document.addAuthor("#####");
            document.addCreator("#####");
        }
       
        private void addTitlePage(Document document, String nummer, String naam, int term, int counted, int company) throws DocumentException {
           
            Paragraph paragraaf = new Paragraph();
            addEmptyLine(paragraaf, 1);
            paragraaf.add(new Paragraph(
                    "Company " + nummer + "\nStreet " + nummer + "\nPostcode " + nummer + " City " + nummer
//                    Reader.getCompany(nummer) + "\n" + Reader.getStreet(nummer) + "\n" + Reader.getPostcode(nummer) + " " + Reader.getCity(nummer)
            ));
            addEmptyLine(paragraaf, 6);
           
            switch(counted) {
                case 0:
                    paragraaf.add(new Paragraph(
                            "Blad: 1                                                                                                                      Oosterblokker, " + datenow +
                            "\nBetreft: herinnering",
                            smallBold));
                    addEmptyLine(paragraaf, 1);
                    if(company == 0)
                        paragraaf.add(new Paragraph("text"));          
                    else
                        paragraaf.add(new Paragraph("text"));          
     
                case 1:
                        paragraaf.add(new Paragraph("text"));          
     
                     addEmptyLine(paragraaf, 1);
                     if(company == 0)
                        paragraaf.add(new Paragraph("text"));          
       
                     else
                        paragraaf.add(new Paragraph("text"));          
     
                default:
                        paragraaf.add(new Paragraph("text"));          
     
            }
     
            addEmptyLine(paragraaf, 2);
            createTable(document, paragraaf, nummer, naam, term, counted, company);
     
           
        }
       
        private void createTable(Document doc, Paragraph paragraaf, String debiteur, String bedrijf, int termijn, int counts, int company) throws DocumentException {
            DecimalFormat f = new DecimalFormat("##.00");
     
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
           
            PdfPCell c1 = new PdfPCell(new Phrase("Factuurnr."));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setBorder(Rectangle.NO_BORDER);
            table.addCell(c1);
     
            c1 = new PdfPCell(new Phrase("Datum"));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setBorder(Rectangle.NO_BORDER);
            table.addCell(c1);
     
            c1 = new PdfPCell(new Phrase("Dagen open"));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setBorder(Rectangle.NO_BORDER);
            table.addCell(c1);
           
            c1 = new PdfPCell(new Phrase("Factuurbedrag"));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setBorder(Rectangle.NO_BORDER);
            table.addCell(c1);
           
            c1 = new PdfPCell(new Phrase("Reeds betaald"));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setBorder(Rectangle.NO_BORDER);
            table.addCell(c1);
           
            c1 = new PdfPCell(new Phrase("Openstaand"));
            c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setBorder(Rectangle.NO_BORDER);
            table.addCell(c1);
           
            table.setHeaderRows(1);
            for(int i = 0; i < /*Reader.getInvoicesList(debiteur).size()*/2; i++) {
                ArrayList<String> list = new ArrayList<String>();
                String invoice = list.get(i);
                if(/*Reader.getPassedDays(invoice) > termijn*/ i%2==0) {
                   
                    c1 = new PdfPCell(new Phrase(invoice));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorder(Rectangle.NO_BORDER);
                    table.addCell(c1);
                   
                    c1 = new PdfPCell(new Phrase(/*Reader.getDate(invoice)*/ "Date " + invoice));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorder(Rectangle.NO_BORDER);
                    table.addCell(c1);
                   
                    c1 = new PdfPCell(new Phrase(/*Reader.getPassedDays(invoice)*/ "PassedDays " + invoice));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorder(Rectangle.NO_BORDER);
                    table.addCell(c1);
                   
                    c1 = new PdfPCell(new Phrase(/*f.format(Reader.getOorspronk(invoice))*/ "Oorspronk " + invoice));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorder(Rectangle.NO_BORDER);
                    table.addCell(c1);          }
                   
                    c1 = new PdfPCell(new Phrase(/*f.format(Reader.getBetaald(invoice))*/ "Betaald " + invoice));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorder(Rectangle.NO_BORDER);
                    table.addCell(c1);
                   
                    c1 = new PdfPCell(new Phrase(/*f.format(Reader.getOpenstaand(invoice))*/ "Openstaand " + invoice));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorder(Rectangle.NO_BORDER);
                    table.addCell(c1);
                   
                   // -------------------------------------------------- 2nd part
                   
                    Font bold = new Font(FontFamily.HELVETICA, 12, Font.BOLD);
                    c1 = new PdfPCell(new Phrase("verwerkt tot   " + datenow, bold));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorder(Rectangle.NO_BORDER);
                    table.addCell(c1);
                   
                   
                    c1 = new PdfPCell(new Phrase());
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorder(Rectangle.NO_BORDER);
                    table.addCell(c1);
                   
                    //trash
                    c1 = new PdfPCell(new Phrase());
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorder(Rectangle.NO_BORDER);        
                    table.addCell(c1);
                   
                    //trash
                    c1 = new PdfPCell(new Phrase());
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorder(Rectangle.NO_BORDER);
                    table.addCell(c1);
                   
                    //Totaalbedrag
                    c1 = new PdfPCell(new Phrase("Totaal:", bold));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorder(Rectangle.NO_BORDER);
                    table.addCell(c1);
                   
                   
                    //Totaalbedrag
                    c1 = new PdfPCell(new Phrase(/*f.format(Reader.getTotaalOpenstaand(debiteur, termijn)) + ""*/ "TotaalOpenstaand " + invoice, bold));
                    c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c1.setBorder(Rectangle.NO_BORDER);
                    table.addCell(c1);
                   
                    paragraaf.add(table);

                        doc.add(paragraaf);

            }
        }
       
        private void addEmptyLine(Paragraph paragraph, int number) {
            for (int i = 0; i < number; i++) {
                paragraph.add(new Paragraph(" "));
            }
        }
       
        private void addline(PdfWriter writer, int counts) {
            PdfContentByte canvas = writer.getDirectContent();
     
           
            if(counts == 0) {
                CMYKColor magentaColor = new CMYKColor(0.58f, 0.17f, 0, 0.56f);
                canvas.setColorStroke(magentaColor);
                canvas.moveTo(38, 420);
                canvas.lineTo(558, 420);
                canvas.moveTo(38, 405);
                canvas.lineTo(558, 405);
                canvas.closePathStroke();
            }
            else if (counts == 1) {
               
                CMYKColor magentaColor = new CMYKColor(0.58f, 0.17f, 0, 0.56f);
                canvas.setColorStroke(magentaColor);
                canvas.moveTo(38, 355);
                canvas.lineTo(558, 355);
                canvas.moveTo(38, 340);
                canvas.lineTo(558, 340);
                canvas.closePathStroke();
            }
            else {
                CMYKColor magentaColor = new CMYKColor(0.58f, 0.17f, 0, 0.56f);
                canvas.setColorStroke(magentaColor);
                canvas.moveTo(38, 420);
                canvas.lineTo(558, 420);
                canvas.moveTo(38, 405);
                canvas.lineTo(558, 405);
                canvas.closePathStroke();
            }
        }
    }
}
