package ai.applica.scanner.cli;

import com.beust.jcommander.Parameter;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Params {

    @Parameter(names = {"-f", "--filename"}, description = "Filename to check in single file mode")
    public Path fileName;

    @Parameter(names = {"-d", "--input-dir"}, description = "Input directory to look for PDF files")
    public Path inputDir;

    @Parameter(names = {"--dump"}, description = "Dump the text and HOCR instead of testing for digital origin")
    public boolean dump = false;

    @Parameter(names = "--break", description = "Should punctuation be treated as words during HOCR dumping")
    public boolean breakOnPunctuation = false;

    @Parameter(names = "--dpi", description = "Pixel depth of virtual image")
    public int dpi = 150;

    @Parameter(names = {"-r", "--recursive"}, description = "Whether to search for PDF files recursively")
    public boolean recursive = false;

    @Parameter(names = {"-o", "--output-file-name"},
            description = "File to write results to. Supported extensions are *.tsv, *.csv")
    public Path outputFileName = Paths.get("results.tsv");

    @Parameter(names = {"-e", "--errors-file-name"},
            description = "File to write errors to. Supported extensions are *.tsv, *.csv")
    public Path errorsFileName = Paths.get("errors.tsv");

    @Parameter(names = {"-v", "--verbose"}, description = "Whether to print processed file names.")
    public boolean verbose = false;

    @Parameter(names = {"--sort"}, description = "Whether to sort file name in results.")
    public boolean sortFileNames = false;

    @Parameter(names = {"-i", "--ignore-list"}, description = "Ignore the files contained in given plain text file.")
    public Path ignoreList;

    @Parameter(names = {"-n", "--n-threads"}, description = "Processing threads count.")
    public int threadCount = 8;

    @Parameter(names = {"-t", "--timeout"}, description = "PDF processing timeout in seconds.")
    public int processingTimeout = 300;

}
