package ai.applica.scanner.model;

import ai.applica.scanner.util.StringCleaner;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Locale;
import java.util.StringJoiner;

@SuppressWarnings({"WeakerAccess", "UnnecessaryLocalVariable"})
public class Result {

    public final String fileName;
    public final boolean hiddenTextPresent;
    public final int textLength;
    public final int hiddenTextLength;
    public final String creator;
    public final String producer;
    public final int pageCount;
    public final double maxCoverRatio;
    public final double avgCoverRatio;
    public final int imageCount;
    public final int objectCount;
    public final String pdfVersion;
    public final boolean hasOutlines;
    public final boolean isTagged;
    public final String lang;
    public final String conformanceLevel;
    public final boolean hasPageLabels;

    public Result(String fileName, boolean hiddenTextPresent, int textLength, int hiddenTextLength, String creator,
                  String producer, int pageCount, double maxCoverRatio, double avgCoverRatio, int imageCount,
                  int objectCount, String pdfVersion, boolean hasOutlines, boolean isTagged, String lang,
                  String conformanceLevel, boolean hasPageLabels) {
        this.fileName = fileName;
        this.hiddenTextPresent = hiddenTextPresent;
        this.textLength = textLength;
        this.hiddenTextLength = hiddenTextLength;
        this.creator = creator;
        this.producer = producer;
        this.pageCount = pageCount;
        this.maxCoverRatio = maxCoverRatio;
        this.avgCoverRatio = avgCoverRatio;
        this.imageCount = imageCount;
        this.objectCount = objectCount;
        this.pdfVersion = pdfVersion;
        this.hasOutlines = hasOutlines;
        this.isTagged = isTagged;
        this.lang = lang;
        this.conformanceLevel = conformanceLevel;
        this.hasPageLabels = hasPageLabels;
    }

    public static String[] fieldNames() {
        String[] fields = {
                "File Name", "Has Hidden Text", "Visible Text Len", "Hidden Text Len", "Creator", "Producer",
                "Page Count", "Max Covered Area Ratio", "Avg Covered Area Ratio", "Image Count", "Object Count",
                "PDF Version", "Has Outlines", "Is Tagged", "Lang", "Conformance Level", "Has Page Labels"
        };
        return fields;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("fileName", fileName)
                .append("hiddenTextPresent", hiddenTextPresent)
                .append("textLength", textLength)
                .append("hiddenTextLength", hiddenTextLength)
                .append("creator", creator)
                .append("producer", producer)
                .append("pageCount", pageCount)
                .append("maxCoverRatio", maxCoverRatio)
                .append("avgCoverRatio", avgCoverRatio)
                .append("imageCount", imageCount)
                .append("objectCount", objectCount)
                .append("pdfVersion", pdfVersion)
                .append("hasOutlines", hasOutlines)
                .append("isTagged", isTagged)
                .append("lang", lang)
                .append("conformanceLevel", conformanceLevel)
                .append("hasPageLabels", hasPageLabels)
                .toString();
    }

    public String toString(String separator) {
        StringJoiner joiner = new StringJoiner(separator);
        joiner.add(fileName);
        joiner.add(Boolean.toString(hiddenTextPresent));
        joiner.add(Integer.toString(textLength));
        joiner.add(Integer.toString(hiddenTextLength));
        joiner.add(StringCleaner.clean(creator));
        joiner.add(StringCleaner.clean(producer));
        joiner.add(Integer.toString(pageCount));
        joiner.add(String.format(Locale.ROOT, "%.3f", maxCoverRatio));
        joiner.add(String.format(Locale.ROOT, "%.3f", avgCoverRatio));
        joiner.add(Integer.toString(imageCount));
        joiner.add(Integer.toString(objectCount));
        joiner.add(StringCleaner.clean(pdfVersion));
        joiner.add(Boolean.toString(hasOutlines));
        joiner.add(Boolean.toString(isTagged));
        joiner.add(StringCleaner.clean(lang));
        joiner.add(StringCleaner.clean(conformanceLevel));
        joiner.add(Boolean.toString(hasPageLabels));

        return joiner.toString();
    }

}
