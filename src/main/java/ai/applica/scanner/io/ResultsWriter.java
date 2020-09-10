package ai.applica.scanner.io;

import ai.applica.scanner.cli.Params;
import ai.applica.scanner.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class ResultsWriter {

    private static final Logger LOG = LoggerFactory.getLogger(ResultsWriter.class);

    private final Path targetFileName;
    private final Path errorsFileName;
    private final String separator;
    private final String errorsSeparator;

    public ResultsWriter(Params params) {
        targetFileName = params.outputFileName;
        errorsFileName = params.errorsFileName;
        separator = getSeparator(params.outputFileName);
        errorsSeparator = getSeparator(params.errorsFileName);
    }

    private String getSeparator(Path pathName) {
        return pathName.toString().endsWith(".tsv") ? "\t" : ";";
    }

    public synchronized void add(Result result) {
        if (targetFileName.toFile().exists())
            append(targetFileName, result.toString(separator).concat("\n").getBytes(UTF_8));
        else
            append(targetFileName, String.join(separator, Result.fieldNames()).concat("\n").getBytes(UTF_8));
    }

    private void append(Path path, byte[] contents) {
        try {
            Files.write(path, contents, CREATE, APPEND);
        } catch (IOException e) {
            LOG.error("Error appending to file {}", path, e);
        }
    }

    public synchronized void addFailed(String fileName, Throwable error) {
        if (errorsFileName.toFile().exists())
            append(errorsFileName, String.format("%s%s%s\n", fileName, errorsSeparator, format(error)).getBytes(UTF_8));
        else
            append(errorsFileName, String.format("%s%s%s\n", "File", errorsSeparator, "Error").getBytes(UTF_8));
    }

    private String format(Throwable error) {
        StringWriter stringWriter = new StringWriter();
        error.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString().replace("\n", "").replace("\r", "");
    }

}
