package ai.applica.scanner.processor;

import ai.applica.scanner.cli.Params;
import ai.applica.scanner.extraction.DumpResult;
import ai.applica.scanner.extraction.PdfDumper;
import ai.applica.scanner.io.PdfFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ContentDumpProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(ContentDumpProcessor.class);

    private final PdfFinder pdfFinder;
    private final int processingTimeout;
    private final ExecutorService executor;
    private final boolean breakOnPunctuation;
    private final int dpi;

    ContentDumpProcessor(Params params) {
        pdfFinder = new PdfFinder(params);
        processingTimeout = params.processingTimeout;
        if (params.threadCount < 2)
            throw new IllegalArgumentException("At least 2 threads required!");
        if (params.threadCount % 2 != 0)
            throw new IllegalArgumentException("Number of threads have to be divisable by 2!");
        executor = Executors.newFixedThreadPool(params.threadCount);
        this.breakOnPunctuation = params.breakOnPunctuation;
        this.dpi = params.dpi;
    }

    @Override
    public void process() {
        pdfFinder.findFiles().forEach(this::processSingle);
        executor.shutdown();
    }

    private void processSingle(Path path) {
        CompletableFuture<Void> dumpPromise = CompletableFuture.runAsync(() -> dump(path), executor);
        CompletableFuture.runAsync(() -> await(path.toString(), dumpPromise), executor);
    }

    private void dump(Path path) {
        PdfDumper pdfDumper = PdfDumper.fromPath(path, breakOnPunctuation, dpi);
        pdfDumper.dump().forEach(DumpResult::save);
    }

    private void await(String fileName, CompletableFuture<Void> dumpPromise) {
        try {
            dumpPromise.get(processingTimeout, SECONDS);
        } catch (Exception e) {
            LOG.error("Error dumping {}", fileName, e);
        }
    }

}
