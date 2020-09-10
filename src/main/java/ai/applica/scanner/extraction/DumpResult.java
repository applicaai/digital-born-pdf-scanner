package ai.applica.scanner.extraction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Objects.requireNonNull;

public class DumpResult {

    private static final Logger LOG = LoggerFactory.getLogger(DumpResult.class);

    private final Path pdfPath;
    private final String contents;
    private final Throwable error;
    private final String extension;

    DumpResult(Path pdfPath, String contents, String extension) {
        this.pdfPath = requireNonNull(pdfPath).toAbsolutePath();
        this.contents = requireNonNull(contents);
        this.extension = requireNonNull(extension);
        this.error = null;
    }

    DumpResult(Path pdfPath, Throwable error, String extension) {
        this.pdfPath = requireNonNull(pdfPath).toAbsolutePath();
        this.contents = "";
        this.error = requireNonNull(error);
        requireNonNull(error.getMessage(), "Error message required!");
        this.extension = requireNonNull(extension);
    }

    public void save() {
        try {
            doSave();
        } catch (IOException e) {
            LOG.error("Error saving dump result for {}", pdfPath, e);
        }
    }

    private void doSave() throws IOException {
        byte[] bytes = error == null ? contents.getBytes(UTF_8) : error.getMessage().getBytes(UTF_8);
        Files.write(buildFilePath(), bytes, CREATE, TRUNCATE_EXISTING);
    }

    private Path buildFilePath() {
        String fileName = pdfPath.toFile().getName();
        String directory = pdfPath.toFile().getParent();
        return Paths.get(directory, String.format("%s.%s", fileName, extension));
    }

}
