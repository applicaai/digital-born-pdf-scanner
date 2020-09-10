package ai.applica.scanner.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public class CommandLineInterface {

    private final String[] args;
    private final Params params;
    private final JCommander commander;

    public CommandLineInterface(String[] args) {
        this.args = args;
        params = new Params();
        commander = new JCommander(params);
        commander.setProgramName("digital-born-pdf-scanner");
    }

    public Params parse() {
        try {
            commander.parse(args);
            validateInput();
            return params;
        } catch (ParameterException pe) {
            System.err.println(pe.getMessage());
            pe.usage();
            System.exit(1);
        }
        return null;
    }

    private void validateInput() {
        Params params = requireNonNull(this.params);
        if (isNull(params.inputDir) && isNull(params.fileName)) {
            ParameterException e = new ParameterException("Either file name or input dir must be provided.");
            e.setJCommander(commander);
            throw e;
        }
    }

}
