package ai.applica.scanner;

import ai.applica.scanner.cli.CommandLineInterface;
import ai.applica.scanner.cli.Params;
import ai.applica.scanner.processor.Processor;
import ai.applica.scanner.processor.ProcessorFactory;

public class Main {

    public static void main(String[] args) {
        CommandLineInterface cli = new CommandLineInterface(args);
        Params params = cli.parse();
        Processor processor = ProcessorFactory.create(params);
        processor.process();
    }

}
