package ai.applica.scanner.processor;

import ai.applica.scanner.cli.Params;

public class ProcessorFactory {

    private ProcessorFactory() {}

    public static Processor create(Params params) {
        return params.dump ? new ContentDumpProcessor(params) : new DigitalOriginScanningProcessor(params);
    }

}
