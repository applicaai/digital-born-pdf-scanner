package ai.applica.scanner.extraction;

import ai.applica.scanner.util.Try;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.FilteredTextEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;

import java.io.IOException;
import java.nio.file.Path;

public class XmlDumper extends BaseDumper {

    public XmlDumper(Path path) throws IOException {
        super(path);
    }

    @Override
    protected Try<String> tryDumpPage(int pageNo) {
        Try<PdfPage> maybePage = Try.of(() -> pdfDocument.getPage(pageNo));
        return maybePage.map(page -> dumpPage(page, pageNo));
    }

    private String dumpPage(PdfPage page, int pageNo) {
        XmlTextFilter xmlTextFilter = new XmlTextFilter(pageNo, page.getPageSize());
        ITextExtractionStrategy strategy = new FilteredTextEventListener(
                new LocationTextExtractionStrategy(), xmlTextFilter);
        // Run extraction, but ignore result
        PdfTextExtractor.getTextFromPage(page, strategy);
        // The real result is in filter
        return xmlTextFilter.getXml();
    }

    @Override
    protected String defaultExtension() {
        return "xml";
    }

    @Override
    protected void writeHeader(StringBuilder sb) {
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<pdf>\n");
    }

    @Override
    protected void writeFooter(StringBuilder sb) {
        sb.append("</pdf>");
    }

}
