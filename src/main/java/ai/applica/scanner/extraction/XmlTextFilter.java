package ai.applica.scanner.extraction;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.filter.IEventFilter;
import com.itextpdf.kernel.pdf.canvas.parser.listener.CharacterRenderInfo;
import org.apache.commons.lang3.StringEscapeUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

@SuppressWarnings("deprecation")
public class XmlTextFilter implements IEventFilter {

    private final NumberFormat formatter;
    private final int pageNo;
    private final Rectangle pageBoundingBox;
    private final List<Word> chunks = new ArrayList<>();

    public XmlTextFilter(int pageNo, Rectangle pageBoundingBox) {
        formatter = NumberFormat.getInstance(Locale.ROOT);
        formatter.setMaximumFractionDigits(4);
        this.pageNo = pageNo;
        this.pageBoundingBox = pageBoundingBox;
    }

    @Override
    public boolean accept(IEventData data, EventType type) {
        if (!type.equals(EventType.RENDER_TEXT))
            return true;
        TextRenderInfo textRenderInfo = (TextRenderInfo) data;
        // hidden text
        if (textRenderInfo.getTextRenderMode() == 3)
            return true;
        final Word chunk = new Word();
        dumpCharacters(chunk, textRenderInfo.getCharacterRenderInfos());
        chunks.add(chunk);

        return false;
    }

    private void dumpCharacters(Word chunk, List<TextRenderInfo> characterRenderInfos) {
        for (TextRenderInfo info : characterRenderInfos) {
            CharacterRenderInfo charInfo = new CharacterRenderInfo(info);
            Character ch = new Character(info.getText(), charInfo.getBoundingBox(),
                    info.getBaseline().getBoundingRectangle(), charInfo.getLocation());
            chunk.addCharacter(ch);
        }
    }

    public String getXml() {
        String chunkz = this.chunks.stream().map(this::chunkToString).collect(joining("\n"));
        return format("<page number=\"%d\" width=\"%s\" height=\"%s\">\n<chunks>\n%s\n</chunks>\n</page>\n",
                pageNo,
                formatter.format(pageBoundingBox.getWidth()),
                formatter.format(pageBoundingBox.getHeight()),
                chunkz);
    }

    private String chunkToString(Word chunk) {
        String chars = chunk.getCharacters().stream().map(this::charToString).collect(joining("\n"));
        return format("<chunk x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\">\n<text>%s</text>\n<chars>\n%s\n</chars>\n</chunk>\n",
                formatter.format(chunk.getBoundingBox().getX()),
                formatter.format(pageBoundingBox.getHeight() - chunk.getBoundingBox().getY()),
                formatter.format(chunk.getBoundingBox().getWidth()),
                formatter.format(chunk.getBoundingBox().getHeight()),
                StringEscapeUtils.escapeXml10(chunk.getText()), chars);
    }

    private String charToString(Character ch) {
        return format("<char x=\"%s\" y=\"%s\" baseline=\"%s\" orientation=\"%s\">%s</char>\n",
                formatter.format(ch.getBoundingBox().getX()),
                formatter.format(pageBoundingBox.getHeight() - ch.getBoundingBox().getY()),
                formatter.format(pageBoundingBox.getHeight() - ch.getBaseline()),
                formatter.format(ch.getOrientation()),
                StringEscapeUtils.escapeXml10(ch.getText()));
    }
    
}
