package mkl.testarea.itext5.content;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfLiteral;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor;

/**
 * <a href="https://stackoverflow.com/questions/66352174/background-image-itextpdf-5-5">
 * Background image itextpdf 5.5
 * </a>
 * <p>
 * This content stream editor removes the former UnderContent from
 * the edited content stream and provides it as list of operations.
 * </p>
 * <p>
 * UnderContent is recognized as a leading block of instructions
 * enveloped in a save-graphics-state/restore-graphics-state frame.
 * </p>
 * 
 * @author mkl
 */
public class UnderContentRemover extends PdfContentStreamEditor {
    /**
     * Clears state of {@link UnderContentRemover}, in particular
     * the collected content. Use this if you use this instance for
     * multiple edit runs.
     */
    public void clear() {
        afterUnderContent = false;
        underContent.clear();
        depth = 0;
    }

    /**
     * Retrieves the collected UnderContent instructions
     */
    public List<List<PdfObject>> getUnderContent() {
        return new ArrayList<List<PdfObject>>(underContent);
    }

    /**
     * Adds the given instructions (which may previously have been
     * retrieved using {@link #getUnderContent()}) to the given
     * {@link PdfContentByte} instance.
     */
    public static void write (PdfContentByte canvas, List<List<PdfObject>> operations) throws IOException {
        for (List<PdfObject> operands : operations) {
            int index = 0;

            for (PdfObject object : operands) {
                object.toPdf(canvas.getPdfWriter(), canvas.getInternalBuffer());
                canvas.getInternalBuffer().append(operands.size() > ++index ? (byte) ' ' : (byte) '\n');
            }
        }
    }

    protected void write(PdfContentStreamProcessor processor, PdfLiteral operator, List<PdfObject> operands) throws IOException {
        String operatorString = operator.toString();
        if (afterUnderContent) {
            super.write(processor, operator, operands);
            return;
        } else if ("q".equals(operatorString)) {
            depth++;
        } else if ("Q".equals(operatorString)) {
            depth--;
            if (depth < 1)
                afterUnderContent = true;
        } else if (depth == 0) {
            afterUnderContent = true;
            super.write(processor, operator, operands);
            return;
        }
        underContent.add(new ArrayList<>(operands));
    }

    boolean afterUnderContent = false;
    List<List<PdfObject>> underContent = new ArrayList<>();
    int depth = 0;
}
