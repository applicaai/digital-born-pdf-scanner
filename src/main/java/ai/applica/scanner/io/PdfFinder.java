package ai.applica.scanner.io;

import ai.applica.scanner.cli.Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class PdfFinder {

    private static final Logger LOG = LoggerFactory.getLogger(PdfFinder.class);

    private final Params params;

    public PdfFinder(Params params) {
        this.params = requireNonNull(params);
    }

    public List<Path> findFiles() {
        final Set<Path> ignoreList = readIgnoreList();
        return searchFiles().stream().filter(p -> !ignoreList.contains(p)).collect(toList());
    }

    private Set<Path> readIgnoreList() {
        if (isNull(params.ignoreList))
            return emptySet();
        try {
            return Files.readAllLines(params.ignoreList).stream()
                    .map(p -> Paths.get(p))
                    .map(Path::toAbsolutePath)
                    .collect(toSet());
        } catch (IOException e) {
            LOG.warn("Unable to read ignore list.", e);
            return emptySet();
        }
    }

    private List<Path> searchFiles() {
        if (nonNull(params.fileName))
            return singletonList(params.fileName);
        if (params.recursive)
            return searchDir();
        return listDir();
    }

    private List<Path> searchDir() {
        try (Stream<Path> files = Files.walk(params.inputDir).filter(this::isPdfFile)) {
            if (params.sortFileNames)
                return files.sorted().collect(toList());
            return files.collect(toList());
        } catch (IOException e) {
            LOG.error("Unable to search for files in the input dir ({}) and its descendants.", params.inputDir, e);
            return emptyList();
        }
    }

    private List<Path> listDir() {
        try (Stream<Path> files = Files.list(params.inputDir).filter(this::isPdfFile)) {
            if (params.sortFileNames)
                return files.sorted().collect(toList());
            return files.collect(toList());
        } catch (IOException e) {
            LOG.error("Unable to list all the files in the input dir ({})", params.inputDir, e);
            return emptyList();
        }
    }

    private boolean isPdfFile(Path file) {
        String fileName = file.toString();
        return file.toFile().isFile() && fileName.toLowerCase(Locale.ROOT).endsWith(".pdf");
    }

}
