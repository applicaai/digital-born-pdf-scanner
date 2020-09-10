package ai.applica.scanner.extraction;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.filter.IEventFilter;
import com.itextpdf.kernel.pdf.canvas.parser.listener.CharacterRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextChunkLocation;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class HocrTextFilter implements IEventFilter {

    private final NumberFormat formatter;
    private final Pattern breakPattern;
    private final double ratio;
    private final int pageNo;
    private final Rectangle pageBox;

    private ITextChunkLocation previousLoc = null;
    private final List<Line> lines = new ArrayList<>();
    private List<Word> currentWords = new ArrayList<>();
    private Word currentWord = null;

    public HocrTextFilter(boolean breakOnPunctuation, int dpi, int pageNo, Rectangle pageBox) {
        this.formatter = NumberFormat.getInstance(Locale.ROOT);
        this.formatter.setMaximumFractionDigits(2);
        this.breakPattern = breakOnPunctuation ? Pattern.compile("\\p{Punct}") : Pattern.compile("\\s");
        this.ratio = dpi / 72d;
        this.pageNo = pageNo;
        this.pageBox = pageBox;
    }

    @Override
    public boolean accept(IEventData data, EventType type) {
        if (!type.equals(EventType.RENDER_TEXT))
            return true;
        TextRenderInfo textRenderInfo = (TextRenderInfo) data;
        // hidden text
        if (textRenderInfo.getTextRenderMode() == 3)
            return true;
        for (TextRenderInfo renderInfo : textRenderInfo.getCharacterRenderInfos())
            dumpCharacters(renderInfo);

        return false;
    }

    private void dumpCharacters(TextRenderInfo renderInfo) {
        CharacterRenderInfo charInfo = new CharacterRenderInfo(renderInfo);
        if (currentWord == null) {
            currentWord = new Word();
        } else if (previousLoc != null && !charInfo.getLocation().sameLine(previousLoc)) {
            currentWords.add(currentWord);
            lines.add(new Line(currentWords));
            currentWords = new ArrayList<>();
            currentWord = new Word();
        } else if (isBreak(renderInfo.getText(), currentWord.getText()) || isBoundary(charInfo.getLocation())) {
            currentWords.add(currentWord);
            currentWord = new Word();
        }
        Character ch = new Character(renderInfo.getText(), getBoundingBox(charInfo),
                renderInfo.getBaseline().getBoundingRectangle(), charInfo.getLocation());
        if (currentWord.isOrientationDifferent(ch.getOrientation())) {
            currentWords.add(currentWord);
            currentWord = new Word();
        }
        currentWord.addCharacter(ch);
        previousLoc = charInfo.getLocation();
    }

    private Rectangle getBoundingBox(CharacterRenderInfo charInfo) {
        Rectangle box = charInfo.getBoundingBox();
        return new Rectangle(box.getX(), pageBox.getHeight() - box.getY(), box.getWidth(), box.getHeight());
    }

    private boolean isBreak(String text, String previousText) {
        return isBlank(text) || isBlank(previousText)
                || breakPattern.matcher(text).matches() || breakPattern.matcher(previousText).matches();
    }

    private boolean isBoundary(ITextChunkLocation location) {
        return previousLoc == null || location.isAtWordBoundary(previousLoc) || !location.sameLine(previousLoc);
    }

    public String getHocr() {
        Collections.sort(lines);
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='ocr_page' id='page_");
        sb.append(pageNo);
        sb.append("' title='bbox ");
        sb.append(renderBoundingBox(pageBox));
        sb.append("; ppageno ");
        sb.append(pageNo - 1);
        sb.append("'>\n");
        renderLines(sb);
        sb.append("</div>\n");
        return sb.toString();
    }

    private void renderLines(StringBuilder sb) {
        int wordOffset = 1;
        for (int lineOffset = 0; lineOffset < lines.size(); lineOffset++) {
            Line line = lines.get(lineOffset);
            sb.append("<p class='ocr_line' id='line_");
            sb.append(pageNo);
            sb.append("_");
            sb.append(lineOffset + 1);
            sb.append("' title='bbox ");
            sb.append(renderBoundingBox(line.getBoundingBox()));
            sb.append("; textangle ");
            List<Word> words = line.getWords();
            String angle = words.isEmpty() ? "0" : formatter.format(180 * words.get(0).getOrientation() / Math.PI);
            sb.append(angle);
            sb.append(";'>\n");
            renderWords(sb, words, wordOffset);
            wordOffset += words.size();
            sb.append("</p>\n");
        }

    }

    private void renderWords(StringBuilder sb, List<Word> words, int offset) {
        words.sort(this::compareWords);
        List<WordEx> wordz = IntStream.range(0, words.size())
                .mapToObj(index -> convertWord(index + offset, words.get(index)))
                .collect(toList());
        correctBoundingBoxes(wordz);
        wordz.stream().map(this::renderWord).forEach(sb::append);
    }

    private int compareWords(Word w1, Word w2) {
        Rectangle box1 = w1.getBoundingBox();
        Rectangle box2 = w2.getBoundingBox();
        int byX = Float.compare(box1.getX(), box2.getX());
        if (byX != 0)
            return byX;
        return -1 * (int) Math.signum(w1.getOrientation()) * Float.compare(box1.getY(), box2.getY());
    }

    private WordEx convertWord(int index, Word word) {
        return new WordEx(index, word.getText(), word.getBoundingBox(), word.getOrientation());
    }

    private void correctBoundingBoxes(List<WordEx> words) {
        WordEx previous = null;
        for (WordEx word: words) {
            if (previous != null && word.angle() == 0d && previous.left > word.left)
                throw new IllegalStateException(format("Words are not in order or intersect: %s; %s.", previous, word));
            if (previous != null && word.angle() == 0d && previous.right > word.left && isBlank(previous.text))
                previous.right = word.left;
            if (previous != null && word.angle() == 0d && previous.right > word.left && isBlank(word.text))
                word.left = previous.right;
            if (previous != null && word.angle() == 0d && previous.right - previous.left <= 0)
                throw new IllegalStateException(format("Word has zero or negative width: %s.", previous));
            previous = word;
        }
    }

    private String renderWord(WordEx word) {
        //noinspection deprecation
        return format("<span class='ocrx_word' id='word_%d_%d' title='bbox %s; x_wconf 100'>%s</span>\n",
                pageNo, word.index, renderBoundingBox(word.left, word.top, word.right, word.bottom),
                StringEscapeUtils.escapeHtml4(word.text));
    }

    private String renderBoundingBox(Rectangle boundingBox) {
        long left = Math.round(boundingBox.getX() * ratio);
        long top = Math.round(boundingBox.getY() * ratio);
        long width = Math.round(boundingBox.getWidth() * ratio);
        long height = Math.round(boundingBox.getHeight() * ratio);
        return renderBoundingBox(left, top, left + width, top + height);
    }

    private String renderBoundingBox(long left, long top, long right, long bottom) {
        return format("%d %d %d %d", left, top, right, bottom);
    }

    private final class WordEx {
        private final int index;
        private final String text;
        private long left;
        private final long top;
        private long right;
        private final long bottom;
        private final double orientation;

        private WordEx(int index, String text, Rectangle rect, double orientation) {
            this.index = index;
            this.text = text;
            this.left = Math.round(rect.getX() * ratio);
            this.top = Math.round(rect.getY() * ratio);
            this.right = this.left + Math.round(rect.getWidth() * ratio);
            this.bottom = this.top + Math.round(rect.getHeight() * ratio);
            this.orientation = orientation;
        }

        double angle() {
            return 180 * orientation / Math.PI;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("word_index", index)
                    .append("text", text)
                    .append("left", left)
                    .append("top", top)
                    .append("right", right)
                    .append("bottom", bottom)
                    .append("orientation", orientation)
                    .toString();
        }

    }

}
