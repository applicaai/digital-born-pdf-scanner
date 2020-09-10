package ai.applica.scanner.extraction;

import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.filter.IEventFilter;

public class HiddenTextFilter implements IEventFilter {

    private boolean hiddenTextPresent = false;

    boolean isHiddenTextPresent() {
        return hiddenTextPresent;
    }

    @Override
    public boolean accept(IEventData data, EventType type) {
        if (!type.equals(EventType.RENDER_TEXT))
            return true;
        int textRenderMode = ((TextRenderInfo) data).getTextRenderMode();
        // is hidden text?
        if (textRenderMode == 3) {
            hiddenTextPresent = true;
            return false;
        }
        return true;
    }

}
