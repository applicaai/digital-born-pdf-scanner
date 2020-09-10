package ai.applica.scanner.extraction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public class PdfDumper {

    private static final Logger LOG = LoggerFactory.getLogger(PdfDumper.class);

    private final Path path;
    private final boolean breakOnPunctuation;
    private final int dpi;

    private PdfDumper(Path path, boolean breakOnPunctuation, int dpi) {
        this.path = path;
        this.breakOnPunctuation = breakOnPunctuation;
        this.dpi = dpi;
    }

    public Stream<DumpResult> dump() {
        try {
            TextDumper textDumper = new TextDumper(path);
            HocrDumper hocrDumper = new HocrDumper(path, breakOnPunctuation, dpi);
            XmlDumper xmlDumper = new XmlDumper(path);
            return Stream.of(textDumper.dump(), hocrDumper.dump(), xmlDumper.dump());
        } catch (IOException e) {
            LOG.error("Error creating dumper.", e);
            return Stream.empty();
        }
    }

    public static PdfDumper fromPath(Path path, boolean breakOnPunctuation, int dpi) {
        return new PdfDumper(path, breakOnPunctuation, dpi);
    }

}
