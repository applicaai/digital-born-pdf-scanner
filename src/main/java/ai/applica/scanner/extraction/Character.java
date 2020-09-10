package ai.applica.scanner.extraction;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.geom.Vector;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextChunkLocation;

class Character implements Bound {

    private static final Vector horizontal = new Vector(1, 0, 0).normalize();

    private final String text;
    private final Rectangle boundingBox;
    private final float baseline;
    private final double orientation;

    Character(String text, Rectangle boundingBox, Rectangle baselineBox, ITextChunkLocation location) {
        this.text = text;
        this.boundingBox = boundingBox;
        this.baseline = baselineBox.getHeight() == 0 ? baselineBox.getY() : baselineBox.getX();

        float dot = location.getEndLocation().subtract(location.getStartLocation()).normalize().dot(horizontal);
        this.orientation = Math.acos(dot) * Math.signum(location.orientationMagnitude());
    }

    public String getText() {
        return text;
    }

    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    public float getBaseline() {
        return baseline;
    }

    public double getOrientation() {
        return orientation;
    }

}
