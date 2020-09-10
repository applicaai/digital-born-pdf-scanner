package ai.applica.scanner.extraction;

import com.itextpdf.kernel.geom.Rectangle;

import java.util.List;

class Line implements Bound {

    private final List<Word> words;

    Line(List<Word> words) {
        this.words = words;
    }

    List<Word> getWords() {
        return words;
    }

    public Rectangle getBoundingBox() {
        return compoundBoundingBox(words);
    }

}
