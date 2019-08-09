package mkl.testarea.itext5.extract;

import com.itextpdf.text.pdf.parser.ExtRenderListener;
import com.itextpdf.text.pdf.parser.Path;
import com.itextpdf.text.pdf.parser.PathConstructionRenderInfo;
import com.itextpdf.text.pdf.parser.PathPaintingRenderInfo;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

/**
 * <a href="https://stackoverflow.com/questions/57013987/extract-text-from-pdf-column-wise-with-column-varying-in-size-and-position">
 * Extract Text from PDF Column wise, with column varying in size and position
 * </a>
 * <br/>
 * <a href="https://www.scribd.com/document/419494453/This-is-a-Sample-Document">
 * 419494453-This-is-a-Sample-Document.pdf
 * </a>
 * <p>
 * This text extraction strategy shows how to customize the
 * {@link SimpleTextExtractionStrategy} to take specialties
 * of the document in question into consideration: It filters
 * unwanted artifacts (single spaces here and there disturbing
 * the recognition of lines by the strategy) and adds a marker
 * for filled paths (the OP's document uses filled paths only
 * for horizontal rules)
 * </p>
 * 
 * @author mkl
 */
public class SimpleDividerAwareTextExtractionStrategy extends SimpleTextExtractionStrategy implements TextExtractionStrategy, ExtRenderListener {
    @Override
    public void renderText(TextRenderInfo renderInfo) {
        if (!" ".equals(renderInfo.getText()))
            super.renderText(renderInfo);
    }

    @Override
    public void modifyPath(PathConstructionRenderInfo renderInfo) { }

    @Override
    public Path renderPath(PathPaintingRenderInfo renderInfo) {
        if (renderInfo.getOperation() == PathPaintingRenderInfo.FILL)
            appendTextChunk("\n----------\n");
        return null;
    }

    @Override
    public void clipPath(int rule) { }
}
