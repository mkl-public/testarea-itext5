package mkl.testarea.itext5.merge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.Matrix;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

/**
 * This {@link RenderListener}looks for vertical sections of use in a page.
 * 
 * @author mkl
 */
public class PageVerticalAnalyzer implements RenderListener
{
    @Override
    public void beginTextBlock() { }
    @Override
    public void endTextBlock() { }

    /*
     * @see RenderListener#renderText(TextRenderInfo)
     */
    @Override
    public void renderText(TextRenderInfo renderInfo)
    {
        LineSegment ascentLine = renderInfo.getAscentLine();
        LineSegment descentLine = renderInfo.getDescentLine();
        float[] yCoords = new float[]{
                ascentLine.getStartPoint().get(Vector.I2),
                ascentLine.getEndPoint().get(Vector.I2),
                descentLine.getStartPoint().get(Vector.I2),
                descentLine.getEndPoint().get(Vector.I2)
        };
        Arrays.sort(yCoords);
        addVerticalUseSection(yCoords[0], yCoords[3]);
    }

    /*
     * @see RenderListener#renderImage(ImageRenderInfo)
     */
    @Override
    public void renderImage(ImageRenderInfo renderInfo)
    {
        Matrix ctm = renderInfo.getImageCTM();
        float[] yCoords = new float[4];
        for (int x=0; x < 2; x++)
            for (int y=0; y < 2; y++)
            {
                Vector corner = new Vector(x, y, 1).cross(ctm);
                yCoords[2*x+y] = corner.get(Vector.I2);
            }
        Arrays.sort(yCoords);
        addVerticalUseSection(yCoords[0], yCoords[3]);
    }

    /**
     * This method marks the given interval as used.
     */
    void addVerticalUseSection(float from, float to)
    {
        if (to < from)
        {
            float temp = to;
            to = from;
            from = temp;
        }

        int i=0, j=0;
        for (; i<verticalFlips.size(); i++)
        {
            float flip = verticalFlips.get(i);
            if (flip < from)
                continue;

            for (j=i; j<verticalFlips.size(); j++)
            {
                flip = verticalFlips.get(j);
                if (flip < to)
                    continue;
                break;
            }
            break;
        }
        boolean fromOutsideInterval = i%2==0;
        boolean toOutsideInterval = j%2==0;

        while (j-- > i)
            verticalFlips.remove(j);
        if (toOutsideInterval)
            verticalFlips.add(i, to);
        if (fromOutsideInterval)
            verticalFlips.add(i, from);
    }

    final List<Float> verticalFlips = new ArrayList<Float>();
}
