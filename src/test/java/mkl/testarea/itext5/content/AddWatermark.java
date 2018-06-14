package mkl.testarea.itext5.content;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
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
}
