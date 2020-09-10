package ai.applica.scanner.util;


import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Utility class to remove control characters as well as NUL bytes from a string.
 */
public class StringCleaner {

    private static final byte NUL = 0;

    /**
     * Cleans given string, that is removes control characters as well as NUL bytes that might be present
     * in certain PDF fields.
     *
     * @param value A string to clean
     * @return Clear string (might be empty)
     */
    public static String clean(String value) {
        if (isBlank(value))
            return EMPTY;
        return removeNulBytes(removeControlCharacters(value));
    }

    private static String removeControlCharacters(String value) {
        if (isEmpty(value)) {
            return value;
        }
        final char[] target = new char[value.length()];
        for (int i = 0; i < value.length(); i++) {
            if (Character.isISOControl(value.charAt(i))) {
                target[i] = ' ';
            } else {
                target[i] = value.charAt(i);
            }
        }
        return new String(target);
    }

    private static String removeNulBytes(String value) {
        byte[] bytes = value.getBytes(UTF_8);
        if (bytes.length == 0)
            return value;
        if (Arrays.binarySearch(bytes, NUL) >= 0)
            return removeNullBytes(bytes);
        return value;
    }

    private static String removeNullBytes(byte[] bytes) {
        final byte[] target = new byte[bytes.length];
        int targetLength = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != NUL) {
                target[i] = bytes[i];
                targetLength++;
            }
        }
        if (targetLength == 0) {
            return EMPTY;
        }
        return new String(target, 0, targetLength, UTF_8);
    }

}
