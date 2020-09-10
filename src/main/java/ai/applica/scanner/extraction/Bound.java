package ai.applica.scanner.extraction;

import com.itextpdf.kernel.geom.Rectangle;

import java.util.Collection;

public interface Bound extends Comparable<Bound> {

    Rectangle getBoundingBox();

    default Rectangle compoundBoundingBox(Collection<? extends Bound> bounds) {
        float minLeft = Float.MAX_VALUE;
        float minTop = Float.MAX_VALUE;
        float maxRight = Float.MIN_VALUE;
        float maxBottom = Float.MIN_VALUE;
        for (Bound bound : bounds) {
            Rectangle boundingBox = bound.getBoundingBox();
            minLeft = Float.min(minLeft, boundingBox.getX());
            minTop = Float.min(minTop, boundingBox.getY());
            maxRight = Float.max(maxRight, boundingBox.getWidth() + boundingBox.getX());
            maxBottom = Float.max(maxBottom, boundingBox.getHeight() + boundingBox.getY());
        }

        return new Rectangle(minLeft, minTop, maxRight - minLeft, maxBottom - minTop);
    }

    default int compareTo(Bound other) {
        int byTop = Double.compare(Math.floor(this.getBoundingBox().getY()), Math.floor(other.getBoundingBox().getY()));
        if (byTop != 0)
            return byTop;
        return Double.compare(Math.floor(this.getBoundingBox().getX()), Math.floor(other.getBoundingBox().getX()));
    }

}
