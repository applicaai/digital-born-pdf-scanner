package ai.applica.scanner.extraction;

import ai.applica.scanner.io.ResultsWriter;
import ai.applica.scanner.model.Result;
import ai.applica.scanner.util.Try;
import ai.applica.scanner.util.TryBoolean;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.FilteredTextEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.OptionalDouble;

import static java.lang.Math.max;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class PdfExtractor {

    private static final String SUCCESS = "\u001B[32mSUCCESS\u001B[0m\n";
    private static final String FAIL = "\u001B[41m\u001B[37mFAILED\u001B[0m\n";

    private final String fileName;
    private final ResultsWriter resultsWriter;
    private final boolean verbose;

    private int visibleTextLength;
    private int hiddenTextLength;
    private boolean containsHiddenText;
    private double coverRatio;
    private double coverRatioSum;
    private int imageCount;

    public PdfExtractor(Path filePath, ResultsWriter resultsWriter, boolean verbose) {
        this.fileName = filePath.toAbsolutePath().toString();
        this.resultsWriter = resultsWriter;
        this.verbose = verbose;
        this.visibleTextLength = 0;
        this.hiddenTextLength = 0;
        this.containsHiddenText = false;
        this.coverRatio = 0d;
        this.coverRatioSum = 0d;
        this.imageCount = 0;
    }

    public void extract() {
        process(getPdfReader()).accept(this::handleSuccess, this::handleFailure);
    }

    private Try<Result> process(Try<PdfReader> maybeReader) {
        if (maybeReader.isFailure())
            return maybeReader.mapError();
        Try<PdfDocument> maybeDocument = maybeReader.map(pdfReader -> {
            pdfReader.setUnethicalReading(true);
            return new PdfDocument(pdfReader);
        });
        if (maybeDocument.isFailure())
            return maybeDocument.mapError();

        Try<PdfDocumentInfo> maybeDocumentInfo = maybeDocument.map(PdfDocument::getDocumentInfo);
        String lang = maybeDocument
                .map(PdfDocument::getCatalog)
                .map(PdfCatalog::getLang)
                .map(PdfString::toUnicodeString)
                .toOptional()
                .orElse(EMPTY);

        String level = maybeReader.map(PdfReader::getPdfAConformanceLevel)
                .map(cl -> format("PDF/A %s%s", cl.getPart(), cl.getConformance()))
                .toOptional()
                .orElse(EMPTY);
        boolean hasPageLabels = maybeDocument.map(PdfDocument::getPageLabels).toOptional().isPresent();

        int numberOfPages = maybeDocument.mapToInt(PdfDocument::getNumberOfPages).orElse(0);
        TryBoolean maybeProcessed = processPages(maybeDocument, numberOfPages);
        if (maybeProcessed.isFailure())
            return maybeProcessed.mapError();

        double avgCoverRatio = numberOfPages > 0 ? coverRatioSum / numberOfPages : 0d;

        Result result =  new Result(fileName,
                containsHiddenText,
                visibleTextLength,
                hiddenTextLength,
                maybeDocumentInfo.map(PdfDocumentInfo::getCreator).toOptional().orElse(EMPTY),
                maybeDocumentInfo.map(PdfDocumentInfo::getProducer).toOptional().orElse(EMPTY),
                numberOfPages,
                coverRatio,
                avgCoverRatio,
                imageCount,
                maybeDocument.mapToInt(PdfDocument::getNumberOfPdfObjects).orElse(0),
                maybeDocument.map(PdfDocument::getPdfVersion).map(PdfVersion::toString).toOptional().orElse(EMPTY),
                maybeDocument.mapToBool(PdfDocument::hasOutlines).getOrDefault(false),
                maybeDocument.mapToBool(PdfDocument::isTagged).getOrDefault(false),
                lang,
                level,
                hasPageLabels
        );
        return Try.of(() -> result);
    }

    private void print(String result) {
        if (verbose)
            System.out.printf("%s Processing \u001B[1m %s %s", LocalDateTime.now(), fileName, result);
    }

    private Try<PdfReader> getPdfReader() {
        try {
            PdfReader pdfReader = new PdfReader(fileName);
            return Try.ofValue(pdfReader);
        } catch (Exception e) {
            return Try.ofError(e);
        }
    }

    private TryBoolean processPages(Try<PdfDocument> maybeDocument, int pageCount) {
        for (int pageNumber = 1; pageNumber <= pageCount; pageNumber++) {
            TryBoolean result = tryProcessPage(maybeDocument, pageNumber);
            if (result.isFailure())
                return result;
        }
        return TryBoolean.success();
    }

    private TryBoolean tryProcessPage(Try<PdfDocument> maybeDocument, int pageNumber) {
        Try<PdfPage> maybePage = maybeDocument.map(document -> document.getPage(pageNumber));
        return maybePage.mapToBool(this::processPage);
    }

    private boolean processPage(PdfPage page) {
        HiddenTextFilter textFilter = new HiddenTextFilter();
        ImageFilter imageFilter = new ImageFilter();
        ITextExtractionStrategy strategy =
                new FilteredTextEventListener(new LocationTextExtractionStrategy(), textFilter, imageFilter);
        Try<String> maybeText = Try.of(() -> PdfTextExtractor.getTextFromPage(page, strategy));
        if (maybeText.isFailure())
            throw new IllegalStateException(maybeText.getException());
        String text = maybeText.get();
        int textLength = isBlank(text) ? 0 : text.length();
        visibleTextLength += textLength;
        String wholePageText = PdfTextExtractor.getTextFromPage(page);
        int wholePageTextLength = isBlank(wholePageText) ? 0 : wholePageText.length();
        hiddenTextLength += (wholePageTextLength - textLength);
        containsHiddenText |= textFilter.isHiddenTextPresent();
        Rectangle pageSize = page.getPageSize();
        double pageArea = pageSize.getHeight() * pageSize.getWidth();
        OptionalDouble maxCoverArea = imageFilter.getImageAreas().stream().mapToDouble(area -> area / pageArea).max();
        coverRatio = max(coverRatio, maxCoverArea.orElse(0d));
        coverRatioSum += maxCoverArea.orElse(0d);
        imageCount += imageFilter.getImageCount();

        return true;
    }

    private void handleSuccess(Result result) {
        resultsWriter.add(result);
        print(SUCCESS);
    }

    private void handleFailure(Throwable error) {
        resultsWriter.addFailed(fileName, error);
        print(FAIL);
    }

}
