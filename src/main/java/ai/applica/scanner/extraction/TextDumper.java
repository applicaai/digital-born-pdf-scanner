package ai.applica.scanner.extraction;

import ai.applica.scanner.util.Try;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.IOException;
import java.nio.file.Path;

public class TextDumper extends BaseDumper {

    public TextDumper(Path pdfPath) throws IOException {
        super(pdfPath);
    }

    protected Try<String> tryDumpPage(int pageNo) {
        Try<PdfPage> maybePage = Try.of(() -> pdfDocument.getPage(pageNo));
        return maybePage.map(page -> dumpPage(page, pageNo));
    }

    private String dumpPage(PdfPage page, int pageNo) {
        String textFromPage = PdfTextExtractor.getTextFromPage(page);
        if (pageNo == pdfDocument.getNumberOfPages())
            return textFromPage;
        return String.format("%s\n", textFromPage);
    }

    @Override
    protected String defaultExtension() {
        return "txt";
    }

}
