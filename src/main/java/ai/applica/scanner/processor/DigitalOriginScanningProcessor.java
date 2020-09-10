package ai.applica.scanner.processor;

import ai.applica.scanner.cli.Params;
import ai.applica.scanner.extraction.PdfExtractor;
import ai.applica.scanner.io.PdfFinder;
import ai.applica.scanner.io.ResultsWriter;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

public class DigitalOriginScanningProcessor implements Processor {

    private final PdfFinder pdfFinder;
    private final ResultsWriter resultsWriter;
    private final boolean verbose;
    private final int processingTimeout;
    private final ExecutorService executor;

    DigitalOriginScanningProcessor(Params params) {
        pdfFinder = new PdfFinder(params);
        resultsWriter = new ResultsWriter(params);
        verbose = params.verbose;
        processingTimeout = params.processingTimeout;
        if (params.threadCount < 2)
            throw new IllegalArgumentException("At least 2 threads required!");
        if (params.threadCount % 2 != 0)
            throw new IllegalArgumentException("Number of threads have to be divisable by 2!");
        executor = Executors.newFixedThreadPool(params.threadCount);
    }

    @Override
    public void process() {
        pdfFinder.findFiles().forEach(this::processSingle);
        executor.shutdown();
    }
    
    private void processSingle(Path path) {
        CompletableFuture<Void> extractionPromise = CompletableFuture.runAsync(() -> extract(path), executor);
        CompletableFuture.runAsync(() -> await(path.toString(), extractionPromise), executor);
    }
    
    private void extract(Path path) {
        PdfExtractor extractor = new PdfExtractor(path, resultsWriter, verbose);
        extractor.extract();
    }

    private void await(String fileName, CompletableFuture<Void> extractionPromise) {
        try {
            extractionPromise.get(processingTimeout, SECONDS);
        } catch (Exception e) {
            resultsWriter.addFailed(fileName, e);
        }
    }

}
