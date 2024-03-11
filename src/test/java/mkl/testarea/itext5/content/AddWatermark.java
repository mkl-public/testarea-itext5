package mkl.testarea.itext5.content;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PatternColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPatternPainter;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class AddWatermark {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/50851199/i-am-unable-to-stamp-content-on-a-pdf-using-itext">
     * I am unable to Stamp Content on a PDF using Itext
     * </a>
     * <p>
     * Cannot reproduce the issue: The result is watermarked, albeit with
     * overlaps, probably due to guessed values of some variables.
     * </p>
     */
    @Test
    public void testAddWatermarkLikeAnonyMous() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("transparency.pdf");
                FileOutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "transparency-watermarked.pdf"))) {
            ByteArrayOutputStream baos = watermarkPDFDocument(resource);
            baos.writeTo(result);
        }
    }

    /**
     * @see #testAddWatermarkLikeAnonyMous()
     */
    ByteArrayOutputStream watermarkPDFDocument (InputStream WatermarkedFileInputStream) throws DocumentException {
        Logger LOG = Logger.getLogger("mkl.testarea.itext5.content.AddWatermark.watermarkPDFDocument(InputStream)");
        Phrase FooterPageNoPhrase;
        Font FooterFont = new Font();
        float CenterFontOpacity = 0.4f;
        float FooterFontOpacity = 0.9f;
        Phrase CenterPhraseDocState = new Phrase("[Released (Expiring)]");
        Phrase CenterPhraseDocLogic = new Phrase("[]");
        float CenterFontSize = 30;
        Phrase FooterPhraseDocState = new Phrase("[Released (Expiring)]");
        Phrase FooterPhraseDocNumber = new Phrase("[92001458 B.3]");
        Phrase FooterPhraseDocName = new Phrase("[Global TMP 2]");
        Phrase FooterPhraseConfidential = new Phrase("[Confidential. Unauthorized use is prohibited.]");

        float FooterDistanceFromLeft = 0;
        float FooterRow1Height = 15;
        float FooterDistanceFromRight = 0;
        float FooterRow2Height = 15;

        ByteArrayOutputStream bytearrayos = new ByteArrayOutputStream();
        try
        {   
            PdfReader reader = new PdfReader(WatermarkedFileInputStream);
            PdfStamper stamper = new PdfStamper(reader, bytearrayos);
            PdfContentByte PdfContentOver; //PDFContent to get page data and to splash over pages. 
            Rectangle pagesize;
            float WatermarkCenterX, WatermarkCenterY;

            // loop over every page
            int TotalNoOfPages = reader.getNumberOfPages();
            for (int i = 1; i <= TotalNoOfPages; i++) {
                // get page size and position
                pagesize = reader.getPageSizeWithRotation(i);
                WatermarkCenterX = (pagesize.getLeft() + pagesize.getRight()) / 2;
                WatermarkCenterY = (pagesize.getTop() + pagesize.getBottom()) / 2;
                LOG.fine("Center of page  - [" + WatermarkCenterX + " , " + WatermarkCenterY+"]");

                //Initializing PageNo Phrase for Footer
                FooterPageNoPhrase = new Phrase("Page " + String.valueOf(i) + " of " + String.valueOf(TotalNoOfPages), FooterFont);

                PdfContentOver = stamper.getOverContent(i);
                PdfContentOver.saveState();

                //Setting Watermark opacity
                PdfGState CenterState = new PdfGState();
                PdfGState FooterState = new PdfGState();
                CenterState.setFillOpacity(CenterFontOpacity);
                FooterState.setFillOpacity(FooterFontOpacity);

                PdfContentOver.setGState(CenterState);//Sets Transparency for Center
                //Watermark at center
                ColumnText.showTextAligned(PdfContentOver, Element.ALIGN_CENTER, CenterPhraseDocState, WatermarkCenterX-(CenterFontSize/2), WatermarkCenterY+(CenterFontSize/2), 45);
                ColumnText.showTextAligned(PdfContentOver, Element.ALIGN_CENTER, CenterPhraseDocLogic, WatermarkCenterX+(CenterFontSize/2), WatermarkCenterY-(CenterFontSize/2), 45);
                LOG.fine("Stamped Center Watermark - " + CenterPhraseDocState + " , " + CenterPhraseDocLogic);
                PdfContentOver.setGState(FooterState); //Sets Transparency for Footer
                //Watermark at Footer Row 1
                ColumnText.showTextAligned(PdfContentOver, Element.ALIGN_LEFT, FooterPhraseDocState, (pagesize.getLeft()+ FooterDistanceFromLeft), (pagesize.getBottom() + FooterRow1Height), 0);
                ColumnText.showTextAligned(PdfContentOver, Element.ALIGN_RIGHT, FooterPhraseDocNumber, (pagesize.getRight()- FooterDistanceFromRight), (pagesize.getBottom() + FooterRow1Height), 0);
                ColumnText.showTextAligned(PdfContentOver, Element.ALIGN_CENTER, FooterPhraseDocName, WatermarkCenterX, (pagesize.getBottom() + FooterRow1Height), 0);
                //Watermark at Footer Row 2
                ColumnText.showTextAligned(PdfContentOver, Element.ALIGN_CENTER, FooterPhraseConfidential, WatermarkCenterX, (pagesize.getBottom() + FooterRow2Height), 0);
                ColumnText.showTextAligned(PdfContentOver, Element.ALIGN_RIGHT, FooterPageNoPhrase, (pagesize.getRight()- FooterDistanceFromRight), (pagesize.getBottom() + FooterRow2Height), 0);
                LOG.fine("Stamped Footer Watermark - " + FooterPhraseDocState + " , " + FooterPhraseDocNumber + " , " + FooterPhraseDocName + " , " + FooterPhraseConfidential);
                PdfContentOver.restoreState();
            }

            stamper.close();
            reader.close();

        }
        catch(IOException e)
        {
            LOG.log(Level.SEVERE, "ERROR while retrieving primary content's file  ", e);
        }
        return bytearrayos;
    }

    /**
     * <a href="https://stackoverflow.com/questions/78135326/unable-to-make-itext5-pdf-watermark-non-removable-in-vmware-workspace-one-boxer">
     * Unable to make itext5 pdf watermark non removable in VMware Workspace ONE Boxer email
     * </a>
     * <p>
     * In contrast to what the OP said, Acrobat can quite easily remove this watermark,
     * simply using the "Edit a PDF" tool, one selects the page-covering object and
     * deletes it. This has to be done some 35 times because the code fills the
     * overcontent that many times with the pattern, but thereafter the mark is gone.
     * </p>
     */
    @Test
    public void testWatermark1LikeMahmoudSaleh() throws IOException, DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf")) {
            byte[] byteArray = IOUtils.toByteArray(resource);
            String watermarkText = "confidential";
//            String fontPath = resourcesPath + "myCustomFont.ttf";
//            Font arabicFont = FontFactory.getFont(fontPath, BaseFont.IDENTITY_H, 16);
//
//            BaseFont baseFont = arabicFont.getBaseFont();
            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA_OBLIQUE, BaseFont.WINANSI, false);

            PdfReader reader = new PdfReader(byteArray);
            PdfStamper stamper = new PdfStamper(reader, baos);

            int numberOfPages = reader.getNumberOfPages();

            float height = baseFont.getAscentPoint(watermarkText, 24) + baseFont.getDescentPoint(watermarkText, 24);

            for (int i = 1; i <= numberOfPages; i++) {

                Rectangle pageSize = reader.getPageSizeWithRotation(i);
                PdfContentByte overContent = stamper.getOverContent(i);

                PdfPatternPainter bodyPainter = stamper.getOverContent(i).createPattern(pageSize.getWidth(),
                        pageSize.getHeight());
                BaseColor baseColor = new BaseColor(10, 10, 10);
                bodyPainter.setColorStroke(baseColor);
                bodyPainter.setColorFill(baseColor);
                bodyPainter.setLineWidth(0.85f);
                bodyPainter.setLineDash(0.2f, 0.2f, 0.2f);

                PdfGState state = new PdfGState();
                state.setFillOpacity(0.3f);
                overContent.saveState();
                overContent.setGState(state);

                for (float x = 70f; x < pageSize.getWidth(); x += height + 100) {
                    for (float y = 90; y < pageSize.getHeight(); y += height + 100) {

                        bodyPainter.beginText();
                        bodyPainter.setTextRenderingMode(PdfPatternPainter.TEXT_RENDER_MODE_FILL);
                        bodyPainter.setFontAndSize(baseFont, 13);
                        bodyPainter.showTextAlignedKerned(Element.ALIGN_MIDDLE, watermarkText, x, y, 45f);
                        bodyPainter.endText();

                        overContent.setColorFill(new PatternColor(bodyPainter));
                        overContent.rectangle(pageSize.getLeft(), pageSize.getBottom(), pageSize.getWidth(),
                                pageSize.getHeight());
                        overContent.fill();
                    }
                }

                overContent.restoreState();
            }

            stamper.close();
            reader.close();
            byteArray = baos.toByteArray();
            File outputFile = new File(RESULT_FOLDER, "Watermark1LikeMahmoudSaleh.pdf");
            if (outputFile.exists()) {
                outputFile.delete();
            }
            Files.write(outputFile.toPath(), byteArray);
        }
    }
}
