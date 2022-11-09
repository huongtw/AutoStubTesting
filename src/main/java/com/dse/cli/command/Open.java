package com.dse.cli.command;

import com.dse.cli.CliEnvironmentLoader;
import com.dse.environment.Environment;
import com.dse.regression.RegressionScriptManager;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;

import static com.dse.cli.command.ICommand.OPEN;

@Command(name = OPEN,
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Open an exist environment from script file.")
public class Open extends AbstractCommand<Environment> {
    @Option(names = {"-e", "--environment"}, paramLabel = "<scriptFile>", arity = "1",
            description = "The script file path archive environment information.")
    private File envScriptFile;
//
//    @Option(names = {"-t", "--testcase"}, paramLabel = "<testcase>", arity = "1",
//            description = "The test case name you need to open.")
//    private String testCase;

    public Open() {
        super();
    }

    @Override
    public Environment call() throws Exception {
        if (envScriptFile != null) {
            logger.info("analyzing the environment script");
            CliEnvironmentLoader.openEnvironment(envScriptFile);

            logger.info("rebuild environment " + Environment.getInstance().getName());
            CliEnvironmentLoader.rebuildEnvironment();

            logger.info("loading test case navigator");
            CliEnvironmentLoader.loadTestCaseTree();

            logger.info("loading regression scripts");
            RegressionScriptManager.getInstance().loadRegressionScripts();

            logger.info("environment ready");
        }

        return Environment.getInstance();
    }


}
