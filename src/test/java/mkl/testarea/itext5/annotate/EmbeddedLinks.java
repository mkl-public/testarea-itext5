package mkl.testarea.itext5.annotate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfDestination;
import com.itextpdf.text.pdf.PdfFileSpecification;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.collection.PdfCollection;
import com.itextpdf.text.pdf.collection.PdfCollectionField;
import com.itextpdf.text.pdf.collection.PdfCollectionItem;
import com.itextpdf.text.pdf.collection.PdfCollectionSchema;
import com.itextpdf.text.pdf.collection.PdfCollectionSort;
import com.itextpdf.text.pdf.collection.PdfTargetDictionary;

/**
 * @author mkl
 */
public class EmbeddedLinks {
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/53440583/linking-pdfs-of-portable-collection-using-itext">
     * Linking pdfs of Portable Collection using Itext
     * </a>
     * <p>
     * This test shows how to link between distinct member documents
     * of a portable collection. The code mostly has been borrowed
     * from the iText portfolio examples <code>KubrickBox</code>,
     * <code>KubrickCollection</code>, and <code>KubrickMovies</code>
     * in a simplified manner.
     * </p>
     * @see #createPdf(String[])
     * @see #createPage(String, String[])
     * @see #getCollectionSchema()
     */
    @Test
    public void testLinkToSibblingInPortfolio() throws IOException, DocumentException {
        Files.write(new File(RESULT_FOLDER, "portfolio-with-embedded-gotos.pdf").toPath(), createPdf(new String[] {"A", "B", "C", "D"}));
    }

    /**
     * @see #testLinkToSibblingInPortfolio()
     */
    public byte[] createPdf(String[] allIds) throws DocumentException, IOException {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("This document contains a collection of PDFs."));

        PdfCollection collection = new PdfCollection(PdfCollection.HIDDEN);
        PdfCollectionSchema schema = getCollectionSchema(); 
        collection.setSchema(schema);
        PdfCollectionSort sort = new PdfCollectionSort("TITLE");
        collection.setSort(sort);
        collection.setInitialDocument("A");
        writer.setCollection(collection);

        PdfFileSpecification fs;
        PdfCollectionItem item;
        for (String id : allIds) {
            fs = PdfFileSpecification.fileEmbedded(writer, null,
                String.format("%s.pdf", id),
                createPage(id, allIds));
            fs.addDescription(id, false);

            item = new PdfCollectionItem(schema);
            item.addItem("TITLE", id);
            fs.addCollectionItem(item);
            writer.addFileAttachment(fs);
        }

        document.close();
        return baos.toByteArray();
    }

    /**
     * @see #testLinkToSibblingInPortfolio()
     */
    public byte[] createPage(String id, String[] allIds) throws DocumentException, IOException {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        Paragraph p = new Paragraph(id,
            FontFactory.getFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED, 48));
        document.add(p);

        for (String linkId : allIds) {
            if (!id.equals(linkId)) {
                PdfTargetDictionary sibbling = new PdfTargetDictionary(true);
                sibbling.setEmbeddedFileName(linkId);
                PdfTargetDictionary parent = new PdfTargetDictionary(sibbling);
                Chunk chunk = new Chunk("Go to " + linkId + ".");
                PdfDestination dest = new PdfDestination(PdfDestination.XYZ, -1, -1, 0);
                dest.addFirst(new PdfNumber(0));
                PdfAction action = PdfAction.gotoEmbedded(null, parent, dest, false);
                chunk.setAction(action);
                document.add(chunk);
            }
        }

        document.close();
        return baos.toByteArray();
    }

    /**
     * @see #testLinkToSibblingInPortfolio()
     */
    private static PdfCollectionSchema getCollectionSchema() {
        PdfCollectionSchema schema = new PdfCollectionSchema();

        PdfCollectionField size = new PdfCollectionField("File size", PdfCollectionField.SIZE);
        size.setOrder(2);
        schema.addField("SIZE", size);

        PdfCollectionField filename = new PdfCollectionField("File name", PdfCollectionField.FILENAME);
        filename.setVisible(false);
        schema.addField("FILE", filename);

        PdfCollectionField title = new PdfCollectionField("Title", PdfCollectionField.TEXT);
        title.setOrder(0);
        schema.addField("TITLE", title);

        return schema;
    }
}
