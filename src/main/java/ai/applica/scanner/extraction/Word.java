package ai.applica.scanner.extraction;

import com.itextpdf.kernel.geom.Rectangle;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

class Word implements Bound {

    private final List<Character> characters = new ArrayList<>();

    void addCharacter(Character character) {
        characters.add(character);
    }

    List<Character> getCharacters() {
        return characters;
    }

    String getText() {
        return characters.stream().map(Character::getText).collect(joining());
    }

    public Rectangle getBoundingBox() {
        return compoundBoundingBox(characters);
    }

    boolean isOrientationDifferent(double angle) {
        if (characters.isEmpty())
            return false;
        return characters.get(0).getOrientation() != angle;
    }

    double getOrientation() {
        if (characters.isEmpty())
            throw new IllegalStateException("This word is empty!");
        return characters.get(0).getOrientation();
    }

}
