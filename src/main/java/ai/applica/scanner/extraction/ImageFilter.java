package ai.applica.scanner.extraction;

import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.filter.IEventFilter;

import java.util.ArrayList;
import java.util.List;

public class ImageFilter implements IEventFilter {

    private final List<Double> imageAreas = new ArrayList<>();
    private int imageCount = 0;

    @Override
    public boolean accept(IEventData data, EventType type) {
        if (type != EventType.RENDER_IMAGE)
            return true;
        ImageRenderInfo info = (ImageRenderInfo) data;
        double area = info.getArea();
        imageAreas.add(area);
        imageCount += 1;

        return true;
    }

    List<Double> getImageAreas() {
        return imageAreas;
    }

    int getImageCount() {
        return imageCount;
    }

}
