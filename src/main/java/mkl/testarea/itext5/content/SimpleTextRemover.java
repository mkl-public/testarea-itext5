package mkl.testarea.itext5.content;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfLiteral;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

/**
 * <a href="https://stackoverflow.com/questions/57308588/replace-text-inside-a-pdf-file-using-itext">
 * Replace text inside a PDF file using iText
 * </a>
 * <p>
 * This content stream editor illustrates what is necessary to
 * properly remove text from a PDF. Actually it is limited in a
 * few aspects:
 * </p>
 * <ul>
 * <li>It only replaces text in the actual page content streams. To also
 * replace text in embedded XObjects, one has to iterate through the
 * XObject resources of the respective page in question recursively and
 * also apply the editor to them.</li>
 * <li>It is "simple" in the same way the SimpleTextExtractionStrategy
 * is: It assumes the text showing instructions to appear in the content
 * in reading order. To also work with content streams for which the
 * order is different and the instructions must be sorted, and this
 * implies that all incoming instructions and relevant render information
 * must be cached until the end of page, not merely a few instruction at
 * a time. Then the render information can be sorted, sections to remove
 * can be identified in the sorted render information, the associated
 * instructions can be manipulated, and the instructions can eventually
 * be stored.</li>
 * <li>It does not try to identify gaps between glyphs that visually
 * represent a white space while there actually is no glyph at all.
 * To identify gaps the code must be extended to check whether two
 * consecutive glyphs exactly follow one another or whether there is
 * a gap or a line jump.</li>
 * <li>When calculating the gap to leave where a glyph is removed, it
 * does not yet take the character and word spacing into account. To
 * improve this, the glyph width calculation must be improved.</li>
 * </ul>
 * <p>
 * Example call:
 * </p>
 * <pre>
 * PdfStamper pdfStamper = ...;
 * SimpleTextRemover remover = new SimpleTextRemover();
 * remover.remove(pdfStamper, i, "Test");
 * </pre>
 * @author mkl
 */
public class SimpleTextRemover extends PdfContentStreamEditor {
    public SimpleTextRemover() {
        super (new SimpleTextRemoverListener());
        ((SimpleTextRemoverListener)getRenderListener()).simpleTextRemover = this;
    }

    /**
     * <p>Removes the string to remove from the given page of the
     * document in the PDF reader the given PDF stamper works on.</p>
     * <p>The result is a list of glyph lists each of which represents
     * a match can can be queried for position information.</p>
     */
    public List<List<Glyph>> remove(PdfStamper pdfStamper, int pageNum, String toRemove) throws IOException {
        if (toRemove.length()  == 0)
            return Collections.emptyList();

        this.toRemove = toRemove;
        cachedOperations.clear();
        elementNumber = -1;
        pendingMatch.clear();
        matches.clear();
        allMatches.clear();
        editPage(pdfStamper, pageNum);
        return allMatches;
    }

    /**
     * Adds the given operation to the cached operations and checks
     * whether some cached operations can meanwhile be processed and
     * written to the result content stream.
     */
    @Override
    protected void write(PdfContentStreamProcessor processor, PdfLiteral operator, List<PdfObject> operands) throws IOException {
        cachedOperations.add(new ArrayList<>(operands));

        while (process(processor)) {
            cachedOperations.remove(0);
        }
    }

    /**
     * Removes any started match and sends all remaining cached
     * operations for processing.
     */
    @Override
    public void finalizeContent() {
        pendingMatch.clear();
        try {
            while (!cachedOperations.isEmpty()) {
                if (!process(this)) {
                    // TODO: Should not happen, so warn
                    System.err.printf("Failure flushing operation %s; dropping.\n", cachedOperations.get(0));
                }
                cachedOperations.remove(0);
            }
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Tries to process the first cached operation. Returns whether
     * it could be processed.
     */
    boolean process(PdfContentStreamProcessor processor) throws IOException {
        if (cachedOperations.isEmpty())
            return false;

        List<PdfObject> operands = cachedOperations.get(0);
        PdfLiteral operator = (PdfLiteral) operands.get(operands.size() - 1);
        String operatorString = operator.toString();

        if (TEXT_SHOWING_OPERATORS.contains(operatorString))
            return processTextShowingOp(processor, operator, operands);

        super.write(processor, operator, operands);
        return true;
    }

    /**
     * Tries to processes a text showing operation. Unless a match
     * is pending and starts before the end of the argument of this
     * instruction, it can be processed. If the instructions contains
     * a part of a match, it is transformed to a TJ operation and
     * the glyphs in question are replaced by text position adjustments.
     * If the original operation had a side effect (jump to next line
     * or spacing adjustment), this side effect is explicitly added.
     */
    boolean processTextShowingOp(PdfContentStreamProcessor processor, PdfLiteral operator, List<PdfObject> operands) throws IOException {
        PdfObject object = operands.get(operands.size() - 2);
        boolean isArray = object instanceof PdfArray;
        PdfArray array = isArray ? (PdfArray) object : new PdfArray(object);
        int elementCount = countStrings(object);

        // Currently pending glyph intersects parameter of this operation -> cannot yet process
        if (!pendingMatch.isEmpty() && pendingMatch.get(0).elementNumber < processedElements + elementCount)
            return false;

        // The parameter of this operation is subject to a match -> copy as is
        if (matches.size() == 0 || processedElements + elementCount <= matches.get(0).get(0).elementNumber || elementCount == 0) {
            super.write(processor, operator, operands);
            processedElements += elementCount;
            return true;
        }

        // The parameter of this operation contains glyphs of a match -> manipulate 
        PdfArray newArray = new PdfArray();
        for (int arrayIndex = 0; arrayIndex < array.size(); arrayIndex++) {
            PdfObject entry = array.getPdfObject(arrayIndex);
            if (!(entry instanceof PdfString)) {
                newArray.add(entry);
            } else {
                PdfString entryString = (PdfString) entry;
                byte[] entryBytes = entryString.getBytes();
                for (int index = 0; index < entryBytes.length; ) {
                    List<Glyph> match = matches.size() == 0 ? null : matches.get(0);
                    Glyph glyph = match == null ? null : match.get(0);
                    if (glyph == null || processedElements < glyph.elementNumber) {
                        newArray.add(new PdfString(Arrays.copyOfRange(entryBytes, index, entryBytes.length)));
                        break;
                    }
                    if (index < glyph.index) {
                        newArray.add(new PdfString(Arrays.copyOfRange(entryBytes, index, glyph.index)));
                        index = glyph.index;
                        continue;
                    }
                    newArray.add(new PdfNumber(-glyph.width));
                    index++;
                    match.remove(0);
                    if (match.isEmpty())
                        matches.remove(0);
                }
                processedElements++;
            }
        }
        writeSideEffect(processor, operator, operands);
        writeTJ(processor, newArray);

        return true;
    }

    /**
     * Counts the strings in the given argument, itself a string or
     * an array containing strings and non-strings.
     */
    int countStrings(PdfObject textArgument) {
        if (textArgument instanceof PdfArray) {
            int result = 0;
            for (PdfObject object : (PdfArray)textArgument) {
                if (object instanceof PdfString)
                    result++;
            }
            return result;
        } else 
            return textArgument instanceof PdfString ? 1 : 0;
    }

    /**
     * Writes side effects of a text showing operation which is going to be
     * replaced by a TJ operation. Side effects are line jumps and changes
     * of character or word spacing.
     */
    void writeSideEffect(PdfContentStreamProcessor processor, PdfLiteral operator, List<PdfObject> operands) throws IOException {
        switch (operator.toString()) {
        case "\"":
            super.write(processor, OPERATOR_Tw, Arrays.asList(operands.get(0), OPERATOR_Tw));
            super.write(processor, OPERATOR_Tc, Arrays.asList(operands.get(1), OPERATOR_Tc));
        case "'":
            super.write(processor, OPERATOR_Tasterisk, Collections.singletonList(OPERATOR_Tasterisk));
        }
    }

    /**
     * Writes a TJ operation with the given array unless array is empty.
     */
    void writeTJ(PdfContentStreamProcessor processor, PdfArray array) throws IOException {
        if (!array.isEmpty()) {
            List<PdfObject> operands = Arrays.asList(array, OPERATOR_TJ);
            super.write(processor, OPERATOR_TJ, operands);
        }
    }

    /**
     * Analyzes the given text render info whether it starts a new match or
     * finishes / continues / breaks a pending match. This method is called
     * by the {@link SimpleTextRemoverListener} registered as render listener
     * of the underlying content stream processor.
     */
    void renderText(TextRenderInfo renderInfo) {
        elementNumber++;
        int index = 0;
        for (TextRenderInfo info : renderInfo.getCharacterRenderInfos()) {
            int matchPosition = pendingMatch.size();
            pendingMatch.add(new Glyph(info, elementNumber, index));
            if (!toRemove.substring(matchPosition, matchPosition + info.getText().length()).equals(info.getText())) {
                reduceToPartialMatch();
            }
            if (pendingMatch.size() == toRemove.length()) {
                matches.add(new ArrayList<>(pendingMatch));
                allMatches.add(new ArrayList<>(pendingMatch));
                pendingMatch.clear();
            }
            index++;
        }
    }

    /**
     * Reduces the current pending match to an actual (partial) match
     * after the addition of the next glyph has invalidated it as a
     * whole match.
     */
    void reduceToPartialMatch() {
        outer:
        while (!pendingMatch.isEmpty()) {
            pendingMatch.remove(0);
            int index = 0;
            for (Glyph glyph : pendingMatch) {
                if (!toRemove.substring(index, index + glyph.text.length()).equals(glyph.text)) {
                    continue outer;
                }
                index++;
            }
            break;
        }
    }

    String toRemove = null;
    final List<List<PdfObject>> cachedOperations = new LinkedList<>();

    int elementNumber = -1;
    int processedElements = 0;
    final List<Glyph> pendingMatch = new ArrayList<>();
    final List<List<Glyph>> matches = new ArrayList<>();
    final List<List<Glyph>> allMatches = new ArrayList<>();

    /**
     * Render listener class used by {@link SimpleTextRemover} as listener
     * of its content stream processor ancestor. Essentially it forwards
     * {@link TextRenderInfo} events and ignores all else.
     */
    static class SimpleTextRemoverListener implements RenderListener {
        @Override
        public void beginTextBlock() { }

        @Override
        public void renderText(TextRenderInfo renderInfo) {
            simpleTextRemover.renderText(renderInfo);
        }

        @Override
        public void endTextBlock() { }

        @Override
        public void renderImage(ImageRenderInfo renderInfo) { }

        SimpleTextRemover simpleTextRemover = null;
    }

    /**
     * Value class representing a glyph with information on
     * the displayed text and its position, the overall number
     * of the string argument of a text showing instruction
     * it is in and the index at which it can be found therein,
     * and the width to use as text position adjustment when
     * replacing it. Beware, the width does not yet consider
     * character and word spacing!
     */
    public static class Glyph {
        public Glyph(TextRenderInfo info, int elementNumber, int index) {
            text = info.getText();
            ascent = info.getAscentLine();
            base = info.getBaseline();
            descent = info.getDescentLine();
            this.elementNumber = elementNumber;
            this.index = index;
            this.width = info.getFont().getWidth(text);
        }

        public final String text;
        public final LineSegment ascent;
        public final LineSegment base;
        public final LineSegment descent;
        final int elementNumber;
        final int index;
        final float width;
    }

    final PdfLiteral OPERATOR_Tasterisk = new PdfLiteral("T*");
    final PdfLiteral OPERATOR_Tc = new PdfLiteral("Tc");
    final PdfLiteral OPERATOR_Tw = new PdfLiteral("Tw");
    final PdfLiteral OPERATOR_Tj = new PdfLiteral("Tj");
    final PdfLiteral OPERATOR_TJ = new PdfLiteral("TJ");
    final static List<String> TEXT_SHOWING_OPERATORS = Arrays.asList("Tj", "'", "\"", "TJ");
    final static Glyph[] EMPTY_GLYPH_ARRAY = new Glyph[0];
}
