package mkl.testarea.itext5.xmlworker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

/**
 * <a href="https://stackoverflow.com/questions/55735723/dynamic-height-size-in-footer-using-itext">
 * Dynamic Height size in footer using iText
 * </a>
 * <p>
 * The {@link Footer} page event listener in this test class demonstrates
 * how to set the margins to match the size of a html footer.
 * </p>
 * 
 * @author mkl
 */
public class StaticFooter {
    final static File RESULT_FOLDER = new File("target/test-outputs", "xmlworker");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * This test case uses a short footer.
     */
    @Test
    public void testShortFooter() throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "StaticFooterShort.pdf")));

        String html = "<p>Short Test Footer.</p>";
        Footer footer = new Footer(html, document.right() - document.left());
        writer.setPageEvent(footer);
        footer.setBottomMargin(document, 10, 10);

        document.open();

        for (int i = 0; i < 200; i++) {
            document.add(new Paragraph("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, ..."));
        }

        document.close();
    }

    /**
     * This test case uses a longer footer.
     */
    @Test
    public void testLongFooter() throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "StaticFooterLong.pdf")));

        String html = "<p>Long Test Footer.</p><p>Long Test Footer.</p><p>Long Test Footer.</p><p>Long Test Footer.</p>";
        Footer footer = new Footer(html, document.right() - document.left());
        writer.setPageEvent(footer);
        footer.setBottomMargin(document, 10, 10);

        document.open();

        for (int i = 0; i < 200; i++) {
            document.add(new Paragraph("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, ..."));
        }

        document.close();
    }

    /**
     * This test case uses an image footer.
     */
    @Test
    public void testImageFooter() throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "StaticFooterImage.pdf")));

        String html = "<img src=\"https://i.stack.imgur.com/VMMeP.jpg?s=328&g=1\"/>";
        Footer footer = new Footer(html, document.right() - document.left());
        writer.setPageEvent(footer);
        footer.setBottomMargin(document, 10, 10);

        document.open();

        for (int i = 0; i < 200; i++) {
            document.add(new Paragraph("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, ..."));
        }

        document.close();
    }

    public static class Footer extends PdfPageEventHelper {
        private final Rectangle boundingBox;
        private final List<Element> elements;
        private PdfTemplate template = null;
        private float blankAfter = 0;

        public Footer(String html, float width) throws DocumentException, IOException {
            this(XMLWorkerHelper.parseToElementList(html, null), width);
        }

        public Footer(List<Element> elements, float width) throws DocumentException, IOException {
            this.elements = elements;
            try (   OutputStream os = new NullOutputStream()    ) {
                Document document = new Document();
                PdfWriter writer = PdfWriter.getInstance(document, os);
                document.open();

                Rectangle bbox = new Rectangle(0, 0, width, 1000);
                float bottomLine = createTemplate(writer, bbox);
                boundingBox = new Rectangle(width, bbox.getTop() - bottomLine);
                template = null;

                writer.setPageEmpty(false);
                document.close();
            }
        }

        float createTemplate(PdfWriter writer, Rectangle boundingBox) throws DocumentException {
            template = writer.getDirectContent().createTemplate(10000, 10000);
            template.setBoundingBox(boundingBox);
            ColumnText columnText = new ColumnText(template);
            Rectangle bbox = template.getBoundingBox();
            columnText.setSimpleColumn(bbox.getLeft(), bbox.getBottom(), bbox.getRight(), bbox.getTop());
            for (Element element : elements) {
                columnText.addElement(element);
            }
            columnText.go();
            return columnText.getYLine() + columnText.getDescender();
        }

        void ensureTemplate(PdfWriter writer) throws DocumentException {
            if (template == null) {
                createTemplate(writer, boundingBox);
            }
        }

        public void setBottomMargin(Document document, float blankBefore, float blankAfter) {
            float marginBottom = boundingBox.getHeight() + blankBefore + blankAfter;
            document.setMargins(document.leftMargin(), document.rightMargin(), document.topMargin(), marginBottom);
            this.blankAfter = blankAfter;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                ensureTemplate(writer);

                Rectangle bbox = template.getBoundingBox();
                Rectangle pageSize = document.getPageSize();

                float x = pageSize.getLeft((pageSize.getWidth() - bbox.getWidth()) / 2f - bbox.getLeft());
                float y = pageSize.getBottom(blankAfter - bbox.getBottom());
                writer.getDirectContentUnder().addTemplate(template, x, y);
            } catch (DocumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            super.onEndPage(writer, document);
        }
    }
}
