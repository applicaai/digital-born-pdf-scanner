package ai.applica.scanner.extraction;

import ai.applica.scanner.util.Try;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDumper implements Dumper {

    private final Path pdfPath;
    protected final PdfDocument pdfDocument;
    protected final StringBuilder builder = new StringBuilder();
    protected final List<Throwable> errors = new ArrayList<>();

    protected BaseDumper(Path pdfPath) throws IOException {
        this.pdfPath = pdfPath;
        this.pdfDocument = new PdfDocument(new PdfReader(pdfPath.toFile()));
    }

    @Override
    public DumpResult dump() {
        writeHeader(builder);
        for (int pageNumber = 1; pageNumber <= pdfDocument.getNumberOfPages(); pageNumber++)
            tryDumpPage(pageNumber).accept(builder::append, errors::add);
        writeFooter(builder);

        return errors.isEmpty() ?
                new DumpResult(pdfPath, builder.toString(), defaultExtension()) :
                new DumpResult(pdfPath, errors.get(0), String.format("%s.%s", defaultExtension(), "error"));
    }

    protected abstract Try<String> tryDumpPage(int pageNo);

    protected abstract String defaultExtension();

    protected void writeHeader(StringBuilder sb) {}

    protected void writeFooter(StringBuilder sb) {}

}
