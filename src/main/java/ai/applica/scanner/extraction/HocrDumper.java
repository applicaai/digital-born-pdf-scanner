package ai.applica.scanner.extraction;

import ai.applica.scanner.util.Try;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.FilteredTextEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;

import java.io.IOException;
import java.nio.file.Path;

public class HocrDumper extends BaseDumper {

    private final boolean breakOnPunctuation;
    private final int dpi;

    public HocrDumper(Path pdfPath, boolean breakOnPunctuation, int dpi) throws IOException {
        super(pdfPath);
        this.breakOnPunctuation = breakOnPunctuation;
        this.dpi = dpi;
    }

    @Override
    protected Try<String> tryDumpPage(int pageNo) {
        Try<PdfPage> maybePage = Try.of(() -> pdfDocument.getPage(pageNo));
        return maybePage.map(page -> dumpPage(page, pageNo));
    }

    private String dumpPage(PdfPage page, int pageNo) {
        HocrTextFilter hocrTextFilter = new HocrTextFilter(breakOnPunctuation, dpi, pageNo, page.getPageSize());
        ITextExtractionStrategy strategy = new FilteredTextEventListener(
                new LocationTextExtractionStrategy(), hocrTextFilter);
        // Run extraction, but ignore result
        PdfTextExtractor.getTextFromPage(page, strategy);
        // The real result is in filter
        return hocrTextFilter.getHocr();
    }

    @Override
    protected String defaultExtension() {
        return "hocr";
    }

    @Override
    protected void writeHeader(StringBuilder sb) {
        sb.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset='UTF-8'>\n");
        sb.append("<meta name='ocr-system' content='digital-born-pdf-scanner-0.0.5'>\n");
        sb.append("<meta name='ocr-capabilities' content='ocr_page ocr_line ocrx_word'>\n");
        sb.append("</head>\n<body>\n");
    }

    @Override
    protected void writeFooter(StringBuilder sb) {
        sb.append("</body>\n</html>\n");
    }

}
