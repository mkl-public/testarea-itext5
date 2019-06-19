package mkl.testarea.itext5.stamp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.io.StreamUtil;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class StampImages {
    final static File RESULT_FOLDER = new File("target/test-outputs", "stamp");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/56650679/how-to-attach-signatures-to-pdf-using-itext">
     * How to attach signatures to pdf using iText?
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1ycajF65sxCqyhSHVctF3x1Y4aml7G8L7">
     * Mix PDF_061820191228.pdf 
     * </a>, with signature and images removed as "Mix PDF.pdf".
     * <p>
     * Cannot reproduce the issue.
     * </p>
     */
    @Test
    public void testStampImagesLikeSubhenduMahanta() throws IOException, DocumentException {
        float CONVERSION_FACTOR_FROM_PIXEL_TO_POINT = 0.75f;
        List<DocumentField> documentField = new ArrayList<>();

        try (   InputStream resource = getClass().getResourceAsStream("Mix PDF.pdf");
                InputStream imageResource = getClass().getResourceAsStream("Signature.png") ) {
            byte[] imageBytes = StreamUtil.inputStreamToArray(imageResource);
            documentField.add(new DocumentField(0, "70", "600", "image", imageBytes));
            documentField.add(new DocumentField(1, "70", "600", "image", imageBytes));
            documentField.add(new DocumentField(2, "70", "600", "image", imageBytes));
            documentField.add(new DocumentField(3, "70", "600", "image", imageBytes));
            documentField.add(new DocumentField(4, "70", "600", "image", imageBytes));

            PdfReader pdfReader = new PdfReader(resource);
            PdfReader.unethicalreading=true;
            PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(new File(RESULT_FOLDER, "StampImagesLikeSubhenduMahanta.pdf")));
            for(DocumentField df:documentField){
                int pageNumber = df.getPageNumber()+1;
                PdfContentByte content = pdfStamper.getOverContent(pageNumber);
                Rectangle cropBox = pdfReader.getCropBox(pageNumber);
                if(pdfReader.getPageRotation(pageNumber) > 0) {
                    float width = cropBox.getRight();
                    cropBox.setRight(cropBox.getHeight());
                    cropBox.setTop(width);                   
                }

                if(df.getFieldType().equals("image")){
                    df.setxPosition(
                        Float.parseFloat(df.getLeft())*
                        CONVERSION_FACTOR_FROM_PIXEL_TO_POINT);
                    df.setyPosition(Float.parseFloat(df.getTop())*CONVERSION_FACTOR_FROM_PIXEL_TO_POINT);
                    float x = cropBox.getLeft()  + df.getxPosition();
                    float y = cropBox.getTop()  - df.getyPosition();
                    Image image = Image.getInstance(df.getFieldValue());
                    image.scaleToFit(150*CONVERSION_FACTOR_FROM_PIXEL_TO_POINT, 50*CONVERSION_FACTOR_FROM_PIXEL_TO_POINT);
                    image.setAbsolutePosition(x, y - 36f);
                    content.addImage(image);
                }else if(df.getFieldType().equals("checkbox")){
                    //...
                }else{
                    //...
                }
            }
            pdfStamper.close(); 
        }
    }

    /** @see StampImages#testStampImagesLikeSubhenduMahanta() */
    class DocumentField {
        DocumentField(int pageNumber, String left, String top, String fieldType, byte[] fieldValue) {
            this.pageNumber = pageNumber;
            this.left = left;
            this.top = top;
            this.fieldType = fieldType;
            this.fieldValue = fieldValue;
        }

        int getPageNumber()                 {   return pageNumber;          }
        final int pageNumber;
        String getLeft()                    {   return left;                }
        final String left;
        String getTop()                     {   return top;                 }
        final String top;
        String getFieldType()               {   return fieldType;           }
        final String fieldType;
        byte[] getFieldValue()              {   return fieldValue;          }
        final byte[] fieldValue;
        float getxPosition()                {   return xPosition;           }
        void setxPosition(float xPosition)  {   this.xPosition = xPosition; }
        float xPosition = 0;
        float getyPosition()                {   return yPosition;           }
        void setyPosition(float yPosition)  {   this.yPosition = yPosition; }
        float yPosition = 0;
    }
}
